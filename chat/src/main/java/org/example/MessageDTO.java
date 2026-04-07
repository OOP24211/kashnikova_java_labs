package org.example;

import java.util.List;

public class MessageDTO {
    public String type;
    public String user;
    public String room;
    public String text;
    public String data;
    public List<String> users;
    public List<String> rooms;
    public List<MessageDTO> messages;
}