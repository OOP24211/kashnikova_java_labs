package org.example;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import com.google.gson.Gson;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer extends WebSocketServer {
    private final Gson gson = new Gson();
    private final HistoryManager historyManager = new HistoryManager();

    private final Map<String, Set<WebSocket>> rooms = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> users = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> userRooms = new ConcurrentHashMap<>();
    private Map<String, List<MessageDTO>> roomHistory;

    public ChatServer(int port) {
        super(new InetSocketAddress(port));

        roomHistory = historyManager.load();
        if (roomHistory == null) {
            roomHistory = new ConcurrentHashMap<>();
        }

        for (String roomName : roomHistory.keySet()) {
            rooms.putIfAbsent(roomName, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }

        if (!rooms.containsKey("room1")) {
            rooms.put("room1", Collections.newSetFromMap(new ConcurrentHashMap<>()));
            roomHistory.put("room1", Collections.synchronizedList(new ArrayList<>()));
        }
    }

    private void broadcastAndSave(MessageDTO msg) {
        roomHistory.putIfAbsent(msg.room, Collections.synchronizedList(new ArrayList<>()));
        roomHistory.get(msg.room).add(msg);

        historyManager.save(roomHistory);

        String json = gson.toJson(msg);
        if (rooms.containsKey(msg.room)) {
            rooms.get(msg.room).forEach(client -> client.send(json));
        }
    }

    private void handleJoin(WebSocket conn, MessageDTO msg) {
        if (userRooms.containsKey(conn)) {
            String oldRoom = userRooms.get(conn);
            if (rooms.containsKey(oldRoom)) {
                rooms.get(oldRoom).remove(conn);
            }
        }

        users.put(conn, msg.user);
        userRooms.put(conn, msg.room);

        rooms.computeIfAbsent(msg.room, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(conn);
        roomHistory.putIfAbsent(msg.room, Collections.synchronizedList(new ArrayList<>()));

        MessageDTO historyResponse = new MessageDTO();
        historyResponse.type = "history";
        historyResponse.messages = new ArrayList<>(roomHistory.get(msg.room));
        conn.send(gson.toJson(historyResponse));

        MessageDTO systemMsg = new MessageDTO();
        systemMsg.type = "system";
        systemMsg.room = msg.room;
        systemMsg.text = msg.user + " вошел в чат";

        roomHistory.get(msg.room).add(systemMsg);
        historyManager.save(roomHistory);

        String jsonNotification = gson.toJson(systemMsg);
        rooms.get(msg.room).forEach(client -> client.send(jsonNotification));

        broadcastUserList(msg.room);
    }

    private void handleCreateRoom(MessageDTO msg) {
        if (msg.room != null && !msg.room.trim().isEmpty()) {
            if (!rooms.containsKey(msg.room)) {
                rooms.put(msg.room, Collections.newSetFromMap(new ConcurrentHashMap<>()));
                roomHistory.put(msg.room, Collections.synchronizedList(new ArrayList<>()));

                historyManager.save(roomHistory);

                broadcastRoomList();
            }
        }
    }

    private void broadcastUserList(String room) {
        if (!rooms.containsKey(room)) return;
        MessageDTO msg = new MessageDTO();
        msg.type = "users";
        msg.users = rooms.get(room).stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
        String json = gson.toJson(msg);
        rooms.get(room).forEach(c -> c.send(json));
    }

    private void sendRoomList(WebSocket conn) {
        MessageDTO msg = new MessageDTO();
        msg.type = "rooms";
        msg.rooms = new ArrayList<>(rooms.keySet());
        conn.send(gson.toJson(msg));
    }

    private void broadcastRoomList() {
        MessageDTO msg = new MessageDTO();
        msg.type = "rooms";
        msg.rooms = new ArrayList<>(rooms.keySet());
        String json = gson.toJson(msg);

        for (WebSocket client : users.keySet()) {
            client.send(json);
        }
    }

    @Override public void onOpen(WebSocket conn, ClientHandshake h) {
        sendRoomList(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String user = users.remove(conn);
        String room = userRooms.remove(conn);
        if (room != null && rooms.containsKey(room)) {
            rooms.get(room).remove(conn);
            broadcastUserList(room);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            MessageDTO msg = gson.fromJson(message, MessageDTO.class);
            switch (msg.type) {
                case "join" -> handleJoin(conn, msg);
                case "message", "file" -> broadcastAndSave(msg);
                case "get_rooms" -> sendRoomList(conn);
                case "create_room" -> handleCreateRoom(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }
    @Override public void onStart() { System.out.println("Сервер успешно запущен на порту 8080"); }

    public static void main(String[] args) { new ChatServer(8080).start(); }
}