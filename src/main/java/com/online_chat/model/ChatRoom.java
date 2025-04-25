package com.online_chat.model;

import java.util.HashSet;
import java.util.Set;

public abstract class ChatRoom {
    protected final String name;
    protected final Set<ClientSession> clients;

    public ChatRoom(String name) {
        this.name = name;
        this.clients = new HashSet<>();
    }

    public int getClientsCount() {
        return clients.size();
    }

    public String getName() {
        return name;
    }

    public void join(ClientSession client) {
        if (canJoin(client)) {
            clients.add(client);
            client.setCurrentRoom(this);
        }
    }

    public void leave(ClientSession client) {
        clients.remove(client);
        if (client.getCurrentRoom() == this) {
            client.setCurrentRoom(null);
        }
    }

    public Set<ClientSession> getClients() {
        return clients;
    }

    public abstract boolean canJoin(ClientSession client);

    public abstract boolean isPublicChatRoom();

}