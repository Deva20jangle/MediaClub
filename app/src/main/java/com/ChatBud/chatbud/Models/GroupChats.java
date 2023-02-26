package com.ChatBud.chatbud.Models;

public class GroupChats {

    String messageID, message, sender, senderName, timestamp, type, url;

    public GroupChats() {
    }

    public GroupChats(String messageID, String message, String sender, String senderName, String timestamp, String type, String url) {
        this.messageID = messageID;
        this.message = message;
        this.sender = sender;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.type = type;
        this.url = url;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
