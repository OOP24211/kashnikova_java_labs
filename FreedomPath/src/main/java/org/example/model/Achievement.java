package org.example.model;

public class Achievement {
    private String title;
    private String description;
    private int requiredDays;
    private boolean unlocked;

    public Achievement(String title, String description, int requiredDays, boolean unlocked) {
        this.title = title;
        this.description = description;
        this.requiredDays = requiredDays;
        this.unlocked = unlocked;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getRequiredDays() { return requiredDays; }
    public boolean isUnlocked() { return unlocked; }

}