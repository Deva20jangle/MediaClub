package com.ChatBud.chatbud.interfaces;

import com.ChatBud.chatbud.Models.Chats;

import java.util.List;

public interface OnReadChatCallBack {
    void onReadSuccess(List<Chats> list);
    void onReadFailed();
}
