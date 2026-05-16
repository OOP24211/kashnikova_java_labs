package org.example.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.*;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class ChatClient extends Application {

    private VBox messageArea;
    private TextField inputField;
    private ListView<String> userListView;
    private ComboBox<String> roomBox;
    private WebSocketClient client;
    private String username;
    private String currentRoom;
    private boolean hasJoinedRoom = false;
    private boolean isUpdatingRooms = false;
    private final Gson gson = new Gson();

    private final String BG_COLOR = "#E6EBEF";
    private final String SIDEBAR_COLOR = "#FFFFFF";
    private final String MY_MESSAGE_COLOR = "#EFFDDE";
    private final String OTHER_MESSAGE_COLOR = "#FFFFFF";
    private final String ACCENT_COLOR = "#2488FF";

    @Override
    public void start(Stage stage) {
        username = new TextInputDialog("User").showAndWait().orElse("User" + (int)(Math.random()*100));

        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setPrefWidth(220);
        leftPanel.setStyle("-fx-background-color: " + SIDEBAR_COLOR + "; -fx-border-color: #DBE1E7; -fx-border-width: 0 1 0 0;");

        Label roomLabel = new Label("Чаты");
        roomLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        roomBox = new ComboBox<>();
        roomBox.setMaxWidth(Double.MAX_VALUE);
        roomBox.setPromptText("Выберите комнату");
        roomBox.setOnAction(e -> switchRoom());

        Button createRoomBtn = new Button("+ Создать комнату");
        createRoomBtn.setMaxWidth(Double.MAX_VALUE);
        createRoomBtn.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-cursor: hand;");
        createRoomBtn.setOnAction(e -> createRoom());

        Label userLabel = new Label("В сети");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        userListView = new ListView<>();
        userListView.setStyle("-fx-background-insets: 0; -fx-padding: 0;");

        leftPanel.getChildren().addAll(roomLabel, roomBox, createRoomBtn, new Separator(), userLabel, userListView);

        messageArea = new VBox(10);
        messageArea.setPadding(new Insets(20));
        messageArea.setStyle("-fx-background-color: " + BG_COLOR + ";");

        ScrollPane scrollPane = new ScrollPane(messageArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: " + BG_COLOR + "; -fx-background-color: transparent;");

        HBox inputPanel = new HBox(10);
        inputPanel.setPadding(new Insets(10, 15, 10, 15));
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setStyle("-fx-background-color: white;");

        Button fileButton = new Button("📎");
        fileButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18; -fx-text-fill: #707579; -fx-cursor: hand;");
        fileButton.setOnAction(e -> sendFile());

        inputField = new TextField();
        inputField.setPromptText("Напишите сообщение...");
        inputField.setPrefHeight(40);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setStyle("-fx-background-radius: 20; -fx-background-color: #F1F1F1; -fx-padding: 0 15 0 15;");
        inputField.setOnAction(e -> sendMessage());

        Button sendButton = new Button("➤");
        sendButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR + "; -fx-font-size: 20; -fx-cursor: hand;");
        sendButton.setOnAction(e -> sendMessage());

        inputPanel.getChildren().addAll(fileButton, inputField, sendButton);

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(scrollPane);
        root.setBottom(inputPanel);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("Telegram Clone - " + username);
        stage.show();

        try { connect(); } catch (Exception e) { e.printStackTrace(); }
    }

    private void addMessageBubble(String user, String text, boolean isOwn, boolean isFile, String fileName, String fileData) {
        VBox bubbleContainer = new VBox(3);
        bubbleContainer.setMaxWidth(400);
        bubbleContainer.setPadding(new Insets(8, 12, 8, 12));

        String bubbleStyle = "-fx-background-radius: 15; " +
                "-fx-background-color: " + (isOwn ? MY_MESSAGE_COLOR : OTHER_MESSAGE_COLOR) + "; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);";
        bubbleContainer.setStyle(bubbleStyle);

        if (!isOwn) {
            Label nameLabel = new Label(user);
            nameLabel.setStyle("-fx-text-fill: " + ACCENT_COLOR + "; -fx-font-weight: bold; -fx-font-size: 12;");
            bubbleContainer.getChildren().add(nameLabel);
        }

        if (isFile) {
            Button btn = new Button("📄 " + fileName);
            btn.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 10; -fx-cursor: hand;");
            btn.setOnAction(e -> downloadFile(fileName, fileData));
            bubbleContainer.getChildren().add(btn);
        } else {
            Text messageText = new Text(text);
            messageText.setWrappingWidth(380);
            bubbleContainer.getChildren().add(messageText);
        }

        HBox alignmentWrapper = new HBox(bubbleContainer);
        alignmentWrapper.setPadding(new Insets(5, 0, 5, 0));
        alignmentWrapper.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Platform.runLater(() -> messageArea.getChildren().add(alignmentWrapper));
    }

    private void addSystemMessage(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10 3 10; -fx-font-size: 11;");
        HBox center = new HBox(label);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10, 0, 10, 0));
        Platform.runLater(() -> messageArea.getChildren().add(center));
    }

    private void connect() throws Exception {
        client = new WebSocketClient(new URI("ws://localhost:8080")) {
            @Override
            public void onOpen(ServerHandshake handshake) { requestRooms(); }

            @Override
            @SuppressWarnings("unchecked")
            public void onMessage(String message) {
                JsonObject json = gson.fromJson(message, JsonObject.class);
                String type = json.get("type").getAsString();

                Platform.runLater(() -> {
                    switch (type) {
                        case "rooms":
                            List<String> rooms = gson.fromJson(json.get("rooms"), List.class);
                            isUpdatingRooms = true;
                            roomBox.setItems(FXCollections.observableArrayList(rooms));
                            isUpdatingRooms = false;
                            break;
                        case "message":
                            addMessageBubble(json.get("user").getAsString(), json.get("text").getAsString(),
                                    json.get("user").getAsString().equals(username), false, null, null);
                            break;
                        case "file":
                            addMessageBubble(json.get("user").getAsString(), null,
                                    json.get("user").getAsString().equals(username), true,
                                    json.get("fileName").getAsString(), json.get("data").getAsString());
                            break;
                        case "system":
                            addSystemMessage(json.get("text").getAsString());
                            break;
                        case "users":
                            List<String> users = gson.fromJson(json.get("users"), List.class);
                            userListView.setItems(FXCollections.observableArrayList(users));
                            break;
                        case "history":
                            messageArea.getChildren().clear();
                            JsonArray history = json.getAsJsonArray("messages");
                            for (JsonElement el : history) {
                                JsonObject m = el.getAsJsonObject();
                                String mType = m.get("type").getAsString();
                                if (mType.equals("message")) {
                                    addMessageBubble(m.get("user").getAsString(), m.get("text").getAsString(),
                                            m.get("user").getAsString().equals(username), false, null, null);
                                } else if (mType.equals("file")) {
                                    addMessageBubble(m.get("user").getAsString(), null,
                                            m.get("user").getAsString().equals(username), true,
                                            m.get("fileName").getAsString(), m.get("data").getAsString());
                                } else {
                                    addSystemMessage(m.get("text").getAsString());
                                }
                            }
                            break;
                    }
                });
            }
            @Override public void onClose(int i, String s, boolean b) {}
            @Override public void onError(Exception e) { e.printStackTrace(); }
        };
        client.connect();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && hasJoinedRoom) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "message");
            json.addProperty("user", username);
            json.addProperty("room", currentRoom);
            json.addProperty("text", text);
            client.send(gson.toJson(json));
            inputField.clear();
        }
    }

    private void sendFile() {
        if (!hasJoinedRoom) return;
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                JsonObject json = new JsonObject();
                json.addProperty("type", "file");
                json.addProperty("user", username);
                json.addProperty("room", currentRoom);
                json.addProperty("fileName", file.getName());
                json.addProperty("data", base64);
                client.send(gson.toJson(json));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void downloadFile(String fileName, String data) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(fileName);
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try {
                Files.write(file.toPath(), Base64.getDecoder().decode(data));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void switchRoom() {
        if (isUpdatingRooms) return;
        String selected = roomBox.getValue();
        if (selected != null && !selected.equals(currentRoom)) {
            currentRoom = selected;
            hasJoinedRoom = true;
            JsonObject json = new JsonObject();
            json.addProperty("type", "join");
            json.addProperty("user", username);
            json.addProperty("room", selected);
            client.send(gson.toJson(json));
        }
    }

    private void createRoom() {
        new TextInputDialog().showAndWait().ifPresent(name -> {
            JsonObject json = new JsonObject();
            json.addProperty("type", "create_room");
            json.addProperty("room", name);
            client.send(gson.toJson(json));
        });
    }

    private void requestRooms() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "get_rooms");
        client.send(gson.toJson(json));
    }

    public static void main(String[] args) { launch(args); }
}