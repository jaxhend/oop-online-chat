package com.online_chat.model;


public class RegularChatRoom extends ChatRoom {

    public RegularChatRoom(String name) {
        super(name);
    }

    @Override
    public boolean canJoin(ClientSession client) {
        return true;
    }

    public boolean isPublicChatRoom() {
        return true;
    }
}