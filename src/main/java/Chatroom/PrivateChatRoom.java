package Chatroom;

import Server.ClientSession;

import java.time.LocalDateTime;
import java.util.List;


public class PrivateChatRoom extends ChatRoom {
    private final List<String> allowedUsers;

    public PrivateChatRoom(String name, String session1, String session2) {
        super(name);
        this.allowedUsers = List.of(session1.toUpperCase(), session2.toUpperCase());
    }

    @Override
    public void join(ClientSession session) {
        if (allowedUsers.contains(session.getUsername().toUpperCase())) {
            addParticipants(session);
            broadcast("liitus ruumiga", session, false);
        } else {
            session.sendMessage("Sul pole õigust selle privaatse vestlusega liituda.");
        }
    }

    @Override
    public boolean canJoin(String username) {
        return allowedUsers.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(username));
    }

    @Override
    public void broadcast(String message, ClientSession session, boolean isChatMessage) {
        String currentTime = LocalDateTime.now().format(timeformatter);
        // kuvab sõnumeid ainult siis, kui teine inimene on ka chatroomis
        if (activeMembers() <= 1 && isChatMessage) {
            String emptyChat = String.format("%s%s%s", RED, "Kedagi teist pole ruumis", RESET);
            session.sendMessage(emptyChat);
            return;
        }
        for (ClientSession participant : getParticipants()) {
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
}
