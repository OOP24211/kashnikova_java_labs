package org.example;

import javafx.application.Application;
import org.example.Client.ChatClient;
import org.example.Server.ChatCommandHandler;
import org.example.Server.ChatServer;
import org.example.Server.HistoryManager;
import org.example.Server.RoomManager;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            startServer();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("client")) {
            startClient();
        } else {
            System.out.println("Запуск всего приложения локально (Сервер + Клиент)...");
            new Thread(Main::startServer).start();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            startClient();
        }
    }

    private static void startServer() {
        RoomManager roomManager = new RoomManager();
        HistoryManager historyManager = new HistoryManager("chat_history.json");
        ChatCommandHandler commandHandler = new ChatCommandHandler(roomManager, historyManager);

        ChatServer server = new ChatServer(8080, commandHandler);
        server.start();
    }

    private static void startClient() {
        Application.launch(ChatClient.class);
    }
}