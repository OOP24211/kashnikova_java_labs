package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HistoryManager {
    private static final String HISTORY_FILE = "chat_history.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public synchronized void save(Map<String, List<MessageDTO>> history) {
        try (Writer writer = new FileWriter(HISTORY_FILE, StandardCharsets.UTF_8)) {
            gson.toJson(history, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public Map<String, List<MessageDTO>> load() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return new HashMap<>();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            java.lang.reflect.Type type = new TypeToken<Map<String, List<MessageDTO>>>(){}.getType();
            Map<String, List<MessageDTO>> data = gson.fromJson(reader, type);
            return data != null ? data : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}