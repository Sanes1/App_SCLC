package com.example.shechaniahinformationsystem;

// ScheduleItem Class
class ScheduleItem {
    private String subject;
    private String time;
    private String days;

    public ScheduleItem(String subject, String time, String days) {
        this.subject = subject;
        this.time = time;
        this.days = days;
    }

    public String getSubject() {
        return subject;
    }

    public String getTime() {
        return time;
    }

    public String getDays() {
        return days;
    }
}
