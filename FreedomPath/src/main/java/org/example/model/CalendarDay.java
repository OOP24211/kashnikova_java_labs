package org.example.model;

import java.time.LocalDate;

public class CalendarDay {
    private LocalDate date;
    private int dayOfMonth;
    private String status;

    public CalendarDay(LocalDate date, int dayOfMonth, String status) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.status = status;
    }

    public LocalDate getDate() { return date; }
    public int getDayOfMonth() { return dayOfMonth; }
    public String getStatus() { return status; }
}