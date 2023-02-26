package com.ChatBud.chatbud.Models;

public class Chats {
    private String chatId;
    private String dateTime;
    private String textMessage;
    private String url;
    private String type;
    private String senderID;
    private String receverID;


    public Chats() {
    }

    public Chats(String chatId, String dateTime, String textMessage, String url, String type, String senderID, String receverID) {
        this.chatId = chatId;
        this.dateTime = dateTime;
        this.textMessage = textMessage;
        this.url = url;
        this.type = type;
        this.senderID = senderID;
        this.receverID = receverID;
    }

    public String getReceverID() {
        return receverID;
    }

    public void setReceverID(String receverID) {
        this.receverID = receverID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}