package Chatroom;

import Client.ClientSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegularChatRoom implements ChatRoom {

    private final String name;

    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    public RegularChatRoom(String name) {
        this.name = name;
    }
    @Override
    public void join(ClientSession session) {
        participants.add(session);
        broadcast(session.getUsername() + " liitus ruumiga " + name);
    }

    @Override
    public void leave(ClientSession session) {
        participants.remove(session);
        broadcast(session.getUsername() + " lahkus ruumist " + name);
    }

    @Override
    public void broadcast(String message) {
        for (ClientSession participant : participants) {
            participant.sendMessage("[" + name + "] " + message);
        }
    }
}
