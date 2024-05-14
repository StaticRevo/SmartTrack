package com.example.smarttrack;

public class DayInfo {
    private String dayOfWeek;
    private String date;
    private String fullDate;

    public DayInfo(String dayOfWeek, String date, String fullDate) {
        this.dayOfWeek = dayOfWeek;
        this.date = date;
        this.fullDate = fullDate;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getDate() {
        return date;
    }


    public String getFullDate() {
        return fullDate;
    }
}
