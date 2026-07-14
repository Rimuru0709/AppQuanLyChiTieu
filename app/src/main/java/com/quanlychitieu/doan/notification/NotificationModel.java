package com.quanlychitieu.doan.notification;

public class NotificationModel {

    public static final String TYPE_REMINDER = "REMINDER";
    public static final String TYPE_ALERT = "ALERT";

    private int id;
    private String title;
    private String message;
    private String type;
    private String createdAt;
    private boolean read;

    public NotificationModel() {
    }

    public NotificationModel(
            int id,
            String title,
            String message,
            String type,
            String createdAt,
            boolean read
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
    }

    public NotificationModel(
            String title,
            String message,
            String type,
            String createdAt,
            boolean read
    ) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isReminder() {
        return TYPE_REMINDER.equals(type);
    }

    public boolean isAlert() {
        return TYPE_ALERT.equals(type);
    }
}