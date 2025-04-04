package Chatroom;

import Client.ClientSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegularChatRoom implements ChatRoom {

    private final String name;
    public static final String RESET = "\033[0m";
    public static final String CYAN = "\033[0;36m";
    public static final String GREEN = "\033[0;32m";
    public static final String WHITE = "\033[0;37m";
    private static final DateTimeFormatter timeformatter = DateTimeFormatter.ofPattern("HH:mm");

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
        String currentTime = LocalDateTime.now().format(timeformatter);

        for (ClientSession participant : participants) {
            participant.sendMessage(
                            CYAN + currentTime + RESET + " [" +
                            GREEN + name + RESET + "] " +
                            WHITE + message + RESET
            );

        }
    }
}
