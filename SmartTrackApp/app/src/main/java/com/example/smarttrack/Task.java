package com.example.smarttrack;
import java.io.Serializable;

public class Task implements Serializable{
    private String description;
    private String color;
    private String date;
    private String time;
    private String endTime;

    public Task(String description, String color, String date, String time, String endTime) {
        this.description = description;
        this.color = color;
        this.date = date;
        this.time = time;
        this.endTime = endTime;
    }

    // Getter methods
    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return time;
    }

    public String getEndTime() {
        return endTime;
    }

    // Setter methods
    public void setDescription(String description) {
        this.description = description;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public void setStartTime(String time) {
        this.time = time;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}