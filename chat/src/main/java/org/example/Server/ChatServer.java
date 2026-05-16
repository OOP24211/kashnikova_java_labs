package org.example.Server;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;

public class ChatServer extends WebSocketServer {
    private final ChatCommandHandler commandHandler;

    public ChatServer(int port, ChatCommandHandler commandHandler) {
        super(new InetSocketAddress(port));
        this.commandHandler = commandHandler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        commandHandler.sendRoomList(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        commandHandler.handleDisconnect(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        commandHandler.handleMessage(conn, message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket сервер успешно запущен.");
    }
}