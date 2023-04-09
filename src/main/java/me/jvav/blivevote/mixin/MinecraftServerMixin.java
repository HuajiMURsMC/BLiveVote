package me.jvav.blivevote.mixin;

import com.mojang.datafixers.util.Pair;
import me.jvav.blivevote.BLiveVote;
import net.minecraft.server.MinecraftServer;
import net.minecraft.voting.votes.FinishedVote;
import net.minecraft.voting.votes.ServerVote;
import net.minecraft.voting.votes.ServerVoteStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract ServerVoteStorage getVoteStorage();

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void onLoadLevel(CallbackInfo ci) {
        BLiveVote.onServerStart((MinecraftServer) (Object) this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onStopServer(CallbackInfo ci) {
        BLiveVote.disconnect();
    }

    @Inject(method = "startVote", at = @At("HEAD"))
    private void onStartVote(UUID uuid, ServerVote serverVote, CallbackInfo ci) {
        BLiveVote.getVoteMap().put(this.getVoteStorage().nextProposalCount() + 1, Pair.of(uuid, serverVote.options().size()));
    }

    @Inject(method = "finishVote(Ljava/util/UUID;Z)Lnet/minecraft/voting/votes/FinishedVote;", at = @At("RETURN"))
    private void onFinishVote(UUID uuid, boolean bl, CallbackInfoReturnable<FinishedVote> cir) {
        BLiveVote.getVoteMap().forEach((k, v) -> {
            if (v.getFirst().equals(uuid)) {
                BLiveVote.getVoteMap().remove(k);
            }
        });
    }
}
