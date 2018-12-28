package com.ganterpore.chatfield.Models;

public class Message {
    public static final int TEXT_TYPE = 0;
    public static final int IMAGE_TYPE = 1;

    private int messageType;
    private String message;
    private String userID;
    private String userName;
    private long sentAt;

    public Message(String message, String userID, String userName, long sentAt) {
        this.message = message;
        this.userID = userID;
        this.userName = userName;
        this.sentAt = sentAt;
        this.messageType = TEXT_TYPE;
    }

    public Message(int messageType, String message, String userID, String userName, long sentAt) {
        this.messageType = messageType;
        this.message = message;
        this.userID = userID;
        this.userName = userName;
        this.sentAt = sentAt;
    }

    public Message() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
