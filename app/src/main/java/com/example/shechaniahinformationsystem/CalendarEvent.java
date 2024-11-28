package com.example.shechaniahinformationsystem;

class CalendarEvent {
    private String event;
    private String date;

    public CalendarEvent(String event, String date) {
        this.event = event;
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public String getDate() {
        return date;
    }
}