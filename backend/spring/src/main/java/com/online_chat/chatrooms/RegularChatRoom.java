package com.online_chat.chatrooms;


import com.online_chat.client.ClientSession;

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