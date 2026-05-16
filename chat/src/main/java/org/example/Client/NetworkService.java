package org.example.Client;

import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class NetworkService {
    private WebSocketClient webSocketClient;
    private NetworkListener listener;

    public void setListener(NetworkListener listener) {
        this.listener = listener;
    }

    public void connect(String url) throws Exception {
        webSocketClient = new WebSocketClient(new URI(url)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                if (listener != null) listener.onConnected();
            }

            @Override
            public void onMessage(String message) {
                if (listener != null) {
                    com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
                    JsonObject json = parser.parse(message).getAsJsonObject();
                    listener.onMessageReceived(json);
                }
            }

            @Override public void onClose(int code, String reason, boolean remote) {}
            @Override public void onError(Exception ex) { ex.printStackTrace(); }
        };
        webSocketClient.connect();
    }

    public void send(String jsonMessage) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(jsonMessage);
        }
    }
}