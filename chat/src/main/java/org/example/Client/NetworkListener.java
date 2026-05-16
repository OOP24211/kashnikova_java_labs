package org.example.Client;

import com.google.gson.JsonObject;

public interface NetworkListener {
    void onMessageReceived(JsonObject json);
    void onConnected();
}