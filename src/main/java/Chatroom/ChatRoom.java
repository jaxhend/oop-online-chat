package Chatroom;

import Client.ClientSession;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ChatRoom {
    private final String name;
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    protected static final String RESET = "\033[0m";
    protected static final String RED = "\033[0;31m";
    protected static final String GREEN = "\033[0;32m";
    protected static final String YELLOW = "\033[0;33m";
    protected static final String BLUE = "\033[0;34m";
    protected static final String PURPLE = "\033[0;35m";
    protected static final String CYAN = "\033[0;36m";
    protected static final String WHITE = "\033[0;37m";
    protected static final DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("HH:mm");

    public ChatRoom(String name) {
        this.name = name;
    }
    public Set<ClientSession> getParticipants() {
        return participants;
    }

    public String getName() {
        return name;
    }


    public abstract void join(ClientSession session);

    public abstract void leave(ClientSession session);

    public abstract void broadcast(String message, ClientSession session, boolean isChatMessage);

    public abstract int activeMembers();

}
