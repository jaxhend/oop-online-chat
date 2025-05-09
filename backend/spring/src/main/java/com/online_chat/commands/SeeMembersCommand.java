package com.online_chat.commands;

import com.online_chat.model.ChatRoom;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.ColoredMessage;

import java.util.List;
import java.util.stream.Collectors;

public class SeeMembersCommand implements Command {

    private final ClientSessionManager sessionManager;

    public SeeMembersCommand(ClientSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public ColoredMessage execute(ClientSession session, String[] args) {
        ChatRoom room = session.getCurrentRoom();
        List<String> usernames;
        if (room == null) {
            usernames = sessionManager.getAllUsernames();
        } else {
            usernames = room.getClients().stream()
                    .map(ClientSession::getUsername)
                    .collect(Collectors.toList());
        }

        return new ColoredMessage("Aktiivsed kasutajad: " + usernames.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", ")), ColoredMessage.COMMANDS);

    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}