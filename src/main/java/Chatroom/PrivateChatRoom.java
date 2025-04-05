package Chatroom;

import Server.ClientSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class PrivateChatRoom extends ChatRoom {
    private final Set<ClientSession> participants = ConcurrentHashMap.newKeySet();
    private final List<String> allowedUsers;

    public PrivateChatRoom(String name, String session1, String session2) {
        super(name);
        this.allowedUsers = List.of(session1, session2);
    }

    @Override
    public void join(ClientSession session) {
        if (allowedUsers.contains(session.getUsername().toUpperCase())) {
            participants.add(session);
            broadcast("liitus ruumiga", session, false);
        }
    }
    public boolean canJoin(String username) {
        return !isPublic() || allowedUsers.contains(username);
    }

    public boolean isPublic(){
        return false;
    }

    @Override
    public void leave(ClientSession session) {
        participants.remove(session);
        if (!participants.isEmpty()) {
            broadcast("lahkus ruumist", session, false);
        }
    }

    @Override
    public void broadcast(String message, ClientSession session, boolean isChatMessage) {
        String currentTime = LocalDateTime.now().format(timeformatter);
        // kuvab sõnumeid ainult siis, kui teine inime on ka chatroomis
        if (activeMembers() <= 1 && isChatMessage) {
            String emptyChat = String.format("%s%s%s", RED, "Kedagi teist pole ruumis", RESET);
            session.sendMessage(emptyChat);
            return;
        }
        for (ClientSession participant : participants) {
            boolean isSender = participant == session;

            if (isChatMessage) {
                String username = isSender ? RED + session.getUsername() + RESET : YELLOW + session.getUsername() + RESET;
                String formattedMessage = String.format("%s%s%s [%s%s%s] %s",
                        CYAN, currentTime, RESET,
                        GREEN, username, RESET,
                        message);
                participant.sendMessage(formattedMessage);
            } else {
                if (isSender) {
                    participant.sendMessage(String.format("%s%s%s %sLiitusite chatroomiga %s%s",
                            CYAN, currentTime, RESET, WHITE, getName(), RESET));
                } else {
                    participant.sendMessage(String.format("%s%s%s [%s%s%s] %s%s%s",
                            CYAN, currentTime, RESET,
                            YELLOW, session.getUsername(), RESET,
                            WHITE, message, RESET));
                }
            }
        }
    }

    @Override
    public int activeMembers() {
        return participants.size();
    }
}
