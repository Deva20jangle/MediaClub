package com.ChatBud.chatbud.Models;


public class ChatList {
    private String userID;
    private String userName;
    private String lastmsg;
    private String date;
    private String urlProfile;
    private String bio;
    private String phno;
    private String FCMToken;

    public ChatList() {
    }

    public ChatList(String userID, String lastmsg) {
        this.userID = userID;
        this.lastmsg = lastmsg;
    }

    public ChatList(String userID, String userName, String date, String urlProfile, String bio, String phno, String FCMToken) {
        this.userID = userID;
        this.userName = userName;
        this.date = date;
        this.urlProfile = urlProfile;
        this.bio = bio;
        this.phno = phno;
        this.FCMToken = FCMToken;
    }

    public String getFCMToken() {
        return FCMToken;
    }

    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getLastmsg() {
        return lastmsg;
    }

    public String getPhno() {
        return phno;
    }

    public void setPhno(String phno) {
        this.phno = phno;
    }

    public void setLastmsg(String lastmsg) {
        this.lastmsg = lastmsg;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String lastmsg() {
        return lastmsg;
    }

    public void lastmsg(String lastmsg) {
        this.lastmsg = lastmsg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrlProfile() {
        return urlProfile;
    }

    public void setUrlProfile(String urlProfile) {
        this.urlProfile = urlProfile;
    }
}