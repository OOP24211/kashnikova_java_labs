package org.example.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.MessageDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryManager {
    private final String historyFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public HistoryManager(String historyFile) {
        this.historyFile = historyFile;
    }

    public synchronized void save(Map<String, List<MessageDTO>> history) {
        try (Writer writer = new FileWriter(historyFile, StandardCharsets.UTF_8)) {
            gson.toJson(history, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения истории: " + e.getMessage());
        }
    }

    public Map<String, List<MessageDTO>> load() {
        File file = new File(historyFile);
        if (!file.exists()) return new ConcurrentHashMap<>();

        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            java.lang.reflect.Type type = new TypeToken<ConcurrentHashMap<String, List<MessageDTO>>>(){}.getType();
            Map<String, List<MessageDTO>> data = gson.fromJson(reader, type);
            return data != null ? data : new ConcurrentHashMap<>();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки истории: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
}