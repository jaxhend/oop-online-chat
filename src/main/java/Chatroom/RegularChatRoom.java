package Chatroom;

import Server.ClientSession;

import java.time.LocalDateTime;

public class RegularChatRoom extends ChatRoom {
    public RegularChatRoom(String name) {
        super(name);
    }

    @Override
    public boolean canJoin(String username) {
        return true;
    }


    @Override
    public void join(ClientSession session) {
        addParticipants(session);
        broadcast(" liitus ruumiga ", session, false);
    }

    @Override
    public void broadcast(String message, ClientSession session, boolean isChatMessage) {
        String currentTime = LocalDateTime.now().format(timeformatter);

        for (ClientSession participant : getParticipants()) {
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
                    participant.sendMessage(String.format("%s%s%s [%s%s%s] %s%s%s%s%s%s",
                            CYAN, currentTime, RESET,
                            GREEN, getName(), RESET,
                            YELLOW, session.getUsername(), RESET, WHITE, message, RESET));
                }
            }
        }
    }
}