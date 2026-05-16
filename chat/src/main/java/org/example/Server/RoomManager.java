package org.example.Server;

import org.java_websocket.WebSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private final Map<String, Set<WebSocket>> rooms = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> users = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> userRooms = new ConcurrentHashMap<>();

    public void addUser(WebSocket conn, String username, String room) {
        users.put(conn, username);
        userRooms.put(conn, room);
        rooms.computeIfAbsent(room, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(conn);
    }

    public String removeUser(WebSocket conn) {
        userRooms.remove(conn);
        return users.remove(conn);
    }

    public String getUserRoom(WebSocket conn) {
        return userRooms.get(conn);
    }

    public String getUsername(WebSocket conn) {
        return users.get(conn);
    }

    public Set<WebSocket> getRoomConnections(String room) {
        return rooms.getOrDefault(room, Collections.emptySet());
    }

    public void removeUserFromRoom(String room, WebSocket conn) {
        if (rooms.containsKey(room)) {
            rooms.get(room).remove(conn);
        }
    }

    public void createRoom(String room) {
        rooms.putIfAbsent(room, Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public Set<String> getAllRooms() {
        return rooms.keySet();
    }

    public Collection<WebSocket> getAllClients() {
        return users.keySet();
    }

    public List<String> getUsersInRoom(String room) {
        if (!rooms.containsKey(room)) return Collections.emptyList();
        return rooms.get(room).stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }
}