package Chatroom;

import Server.ClientSession;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ChatRoom {
    // Mõeldud terminali chati värvimiseks
    protected static final String RESET = "\033[0m";
    protected static final String RED = "\033[0;31m";
    protected static final String GREEN = "\033[0;32m";
    protected static final String YELLOW = "\033[0;33m";
    protected static final String CYAN = "\033[0;36m";
    protected static final String WHITE = "\033[0;37m";
    protected static final DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("HH:mm");
    private final String name;
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    public ChatRoom(String name) {
        this.name = name;
    }

    public Set<ClientSession> getParticipants() {
        return participants;
    }

    public void removeParticipants(ClientSession clientSession) {
        participants.remove(clientSession);
    }

    public void addParticipants(ClientSession clientSession) {
        participants.add(clientSession);
    }

    public String getName() {
        return name;
    }

    public int activeMembers() {
        return participants.size();
    }

    public void leave(ClientSession session) {
        removeParticipants(session);
        if (!getParticipants().isEmpty()) {
            broadcast(" lahkus ruumist ", session, false);
        }
    }

    public abstract void join(ClientSession session);

    public abstract void broadcast(String message, ClientSession session, boolean isChatMessage);

    public abstract boolean canJoin(String username);
}
