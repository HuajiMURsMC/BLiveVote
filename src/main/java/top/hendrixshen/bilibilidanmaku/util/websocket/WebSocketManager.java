package top.hendrixshen.bilibilidanmaku.util.websocket;

import lombok.extern.slf4j.Slf4j;
import me.jvav.blivevote.BLiveVote;

@Slf4j
public class WebSocketManager {
    private static WebSocketClient webSocketClient = null;

    public static WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public static void open() {
        if (webSocketClient == null) {
            webSocketClient = new WebSocketClient();
            try {
                webSocketClient.open();
                BLiveVote.broadcast("Websocket 连接成功!");
            } catch (Exception e) {
                webSocketClient = null;
                BLiveVote.broadcast("Websocket 连接失败!");
                log.error(e.toString());
            }
        }
    }

    public static void close() {
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (InterruptedException e) {
                log.error(e.toString());
            } finally {
                BLiveVote.broadcast("Websocket 已断开!");
                webSocketClient = null;
            }
        }
    }
}
