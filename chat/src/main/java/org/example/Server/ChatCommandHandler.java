package org.example.Server;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.example.MessageDTO;

import java.util.*;

public class ChatCommandHandler {
    private final Gson gson = new Gson();
    private final RoomManager roomManager;
    private final HistoryManager historyManager;
    private final Map<String, List<MessageDTO>> roomHistory;

    public ChatCommandHandler(RoomManager roomManager, HistoryManager historyManager) {
        this.roomManager = roomManager;
        this.historyManager = historyManager;
        this.roomHistory = historyManager.load();

        // Инициализируем комнаты из истории
        for (String roomName : roomHistory.keySet()) {
            roomManager.createRoom(roomName);
        }
        if (!roomHistory.containsKey("room1")) {
            roomManager.createRoom("room1");
            roomHistory.put("room1", Collections.synchronizedList(new ArrayList<>()));
        }
    }

    public void handleMessage(WebSocket conn, String message) {
        MessageDTO msg = gson.fromJson(message, MessageDTO.class);
        switch (msg.type) {
            case "join" -> handleJoin(conn, msg);
            case "message", "file" -> handleBroadcastMessage(msg);
            case "get_rooms" -> sendRoomList(conn);
            case "create_room" -> handleCreateRoom(msg);
        }
    }

    private void handleJoin(WebSocket conn, MessageDTO msg) {
        String oldRoom = roomManager.getUserRoom(conn);
        if (oldRoom != null) {
            roomManager.removeUserFromRoom(oldRoom, conn);
        }

        roomManager.addUser(conn, msg.user, msg.room);
        roomHistory.putIfAbsent(msg.room, Collections.synchronizedList(new ArrayList<>()));

        // Отправка истории
        MessageDTO historyResponse = new MessageDTO();
        historyResponse.type = "history";
        historyResponse.messages = new ArrayList<>(roomHistory.get(msg.room));
        conn.send(gson.toJson(historyResponse));

        // Системное уведомление
        MessageDTO systemMsg = new MessageDTO();
        systemMsg.type = "system";
        systemMsg.room = msg.room;
        systemMsg.text = msg.user + " вошел в чат";

        roomHistory.get(msg.room).add(systemMsg);
        historyManager.save(roomHistory);

        broadcastToRoom(msg.room, systemMsg);
        broadcastUserList(msg.room);
    }

    private void handleBroadcastMessage(MessageDTO msg) {
        roomHistory.putIfAbsent(msg.room, Collections.synchronizedList(new ArrayList<>()));
        roomHistory.get(msg.room).add(msg);
        historyManager.save(roomHistory);
        broadcastToRoom(msg.room, msg);
    }

    private void handleCreateRoom(MessageDTO msg) {
        if (msg.room != null && !msg.room.trim().isEmpty()) {
            roomManager.createRoom(msg.room);
            roomHistory.putIfAbsent(msg.room, Collections.synchronizedList(new ArrayList<>()));
            historyManager.save(roomHistory);
            broadcastRoomList();
        }
    }

    public void handleDisconnect(WebSocket conn) {
        String room = roomManager.getUserRoom(conn);
        String user = roomManager.removeUser(conn);
        if (room != null) {
            roomManager.removeUserFromRoom(room, conn);
            broadcastUserList(room);
        }
    }

    public void sendRoomList(WebSocket conn) {
        MessageDTO msg = new MessageDTO();
        msg.type = "rooms";
        msg.rooms = new ArrayList<>(roomManager.getAllRooms());
        conn.send(gson.toJson(msg));
    }

    private void broadcastToRoom(String room, MessageDTO msg) {
        String json = gson.toJson(msg);
        roomManager.getRoomConnections(room).forEach(client -> client.send(json));
    }

    private void broadcastUserList(String room) {
        MessageDTO msg = new MessageDTO();
        msg.type = "users";
        msg.users = roomManager.getUsersInRoom(room);
        broadcastToRoom(room, msg);
    }

    private void broadcastRoomList() {
        MessageDTO msg = new MessageDTO();
        msg.type = "rooms";
        msg.rooms = new ArrayList<>(roomManager.getAllRooms());
        String json = gson.toJson(msg);
        roomManager.getAllClients().forEach(client -> client.send(json));
    }
}