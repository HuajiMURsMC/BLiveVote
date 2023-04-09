package me.jvav.blivevote;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.jvav.blivevote.mixin.ServerVoteStorageAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.VoterMap;
import top.hendrixshen.bilibilidanmaku.util.websocket.WebSocketManager;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Slf4j
public class BLiveVote {
    @Getter
    private static int roomId;

    @Getter
    private static MinecraftServer server;

    @Getter
    private static final Map<Integer, Pair<UUID, Integer>> voteMap = new HashMap<>();

    private final static String HELP_MESSAGE = """
        /bvote 获取帮助信息
        /bvote connect <房间号> 连接到直播间
        /bvote disconnect 断开连接""";

    public static void onServerStart(MinecraftServer server) {
        BLiveVote.server = server;
        disconnect();
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
            literal("bvote")
                .requires(context -> context.hasPermission(4))
                .then(
                    literal("connect")
                        .then(
                            argument("room_id", integer())
                                .executes(context -> {
                                    roomId = getInteger(context, "room_id");
                                    connect();
                                    return 1;
                                })
                        )
                )
                .then(
                    literal("disconnect")
                        .executes(context -> {
                            disconnect();
                            return 1;
                        })
                )
                .executes(context -> {
                    for (String line : HELP_MESSAGE.split("\n")) {
                        context.getSource().sendSystemMessage(Component.literal(line));
                    }
                    return 1;
                })
        );
    }

    public static void handleDanmaku(String username, String danmaku, boolean isGuard) {
        log.info("收到来自 %s 的弹幕: %s".formatted(isGuard ? "舰长 %s".formatted(username) : username, danmaku));
        if (!danmaku.startsWith("#")) {
            return;
        }
        String[] vote = danmaku.substring(1).split("-");
        int voteId = Integer.parseInt(vote[0]);
        UUID uuid = UUID.nameUUIDFromBytes("BiliUser:%s".formatted(username).getBytes(StandardCharsets.UTF_8));
        int operationId = Integer.parseInt(vote[1]);
        OptionId optionId = new OptionId(voteMap.get(voteId).getFirst(), operationId - 1);

        VoterMap votes = ((ServerVoteStorageAccessor) server.getVoteStorage()).getVotes();
        if (votes.getVotes(optionId, uuid) != 0) {
            return;
        }
        votes.addVote(optionId, uuid, Component.literal(username), 1);

        broadcast(
            Component.empty()
                .append(
                    Component
                        .literal(username)
                        .withStyle(
                            isGuard ? ChatFormatting.GOLD : ChatFormatting.WHITE,
                            ChatFormatting.BOLD
                        )
                ).append(
                    Component
                        .literal(" 投票了 #%d-%d".formatted(voteId, operationId))
                        .withStyle(
                            ChatFormatting.BOLD,
                            ChatFormatting.ITALIC
                        )
                )
        );
    }

    public static void connect() {
        if (WebSocketManager.getWebSocketClient() != null) {
            WebSocketManager.close();
        }
        WebSocketManager.open();
    }

    public static void disconnect() {
        if (WebSocketManager.getWebSocketClient() != null) {
            WebSocketManager.close();
        }
    }

    public static void broadcast(Component component) {
        server.getPlayerList().broadcastSystemMessage(component, false);
    }

    public static void broadcast(String string) {
        broadcast(Component.literal(string));
    }
}
