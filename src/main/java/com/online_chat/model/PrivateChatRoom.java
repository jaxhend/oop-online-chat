package com.online_chat.model;

import java.util.List;

public class PrivateChatRoom extends ChatRoom {

    private final List<String> allowedUsers;

    public PrivateChatRoom(String name, ClientSession owner, ClientSession invited) {
        super(name);
        this.allowedUsers = List.of(
                owner.getUsername().toLowerCase(),
                invited.getUsername().toLowerCase()
        );
    }
    // kontrollime kas kasutaja tohib ruumiga liituda
    @Override
    public boolean canJoin(ClientSession client) {
        return client.getUsername() != null &&
                allowedUsers.contains(client.getUsername().toLowerCase());
    }
    public boolean isPublicChatRoom() {
        return false;
    }

}