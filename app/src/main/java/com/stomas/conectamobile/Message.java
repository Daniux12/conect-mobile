package com.stomas.conectamobile;

public class Message {
    private String text;
    private String senderId;

    public Message() {
        // Constructor vacío necesario para Firebase
    }

    public Message(String text, String senderId) {
        this.text = text;
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
