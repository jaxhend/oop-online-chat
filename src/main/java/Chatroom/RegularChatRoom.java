package Chatroom;

import Server.ClientSession;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegularChatRoom extends ChatRoom {
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();

    public RegularChatRoom(String name) {
        super(name);
    }
    public boolean canJoin(String username) {
        return true;
    }

    @Override
    public Set<ClientSession> getParticipants() {
        return participants;
    }

    public void join(ClientSession session) {
        participants.add(session);
        broadcast(" liitus ruumiga ", session, false);
    }

    @Override
    public void leave(ClientSession session) {
        participants.remove(session);
        if (!participants.isEmpty()) {
            broadcast(" lahkus ruumist ", session, false);
        }
    }

    @Override
    public int activeMembers() {
        return participants.size();
    }

    @Override
    public void broadcast(String message, ClientSession session, boolean isChatMessage) {
        String currentTime = LocalDateTime.now().format(timeformatter);

        for (ClientSession participant : participants) {
            boolean isSender = participant == session;

            if (isChatMessage) {
                String username = isSender ? RED + session.getUsername() + RESET : YELLOW + session.getUsername() + RESET;
                String formattedMessage = String.format("%s%s%s [%s%s%s] %s: %s",
                        CYAN, currentTime, RESET,
                        GREEN, getName(), RESET,
                        username, message);
                participant.sendMessage(formattedMessage);
            } else {
                if (isSender) {
                    participant.sendMessage(String.format("%s%s%s %sLiitusite chatroomiga %s%s",
                            CYAN, currentTime, RESET, WHITE, getName(), RESET));
                } else {
                    participant.sendMessage(String.format("%s%s%s [%s%s%s] %s%s%s%s%s",
                            CYAN, currentTime, RESET,
                            GREEN, getName(), RESET,
                            YELLOW, session.getUsername(), RESET, WHITE, message, RESET));
                }
            }
        }
    }
}