package com.quanlychitieu.doan.chatbot;

public class ChatMessage {

    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    private final String message;
    private final int type;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public boolean isUserMessage() {
        return type == TYPE_USER;
    }
}