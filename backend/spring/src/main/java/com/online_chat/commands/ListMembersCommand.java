package com.online_chat.commands;

import com.online_chat.chatrooms.ChatRoom;
import com.online_chat.client.ClientSession;
import com.online_chat.client.ClientSessionManager;
import com.online_chat.model.MessageFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class ListMembersCommand implements Command {

    private final ClientSessionManager sessionManager;

    public ListMembersCommand(ClientSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        ChatRoom room = session.getCurrentRoom();
        List<String> usernames;
        if (room == null) {
            usernames = sessionManager.getAllUsernames();
            return new MessageFormatter("Aktiivsed kasutajad veebilehel: " + usernames.stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining(", ")), MessageFormatter.PURPLE);
        } else {
            usernames = room.getClients().stream()
                    .map(ClientSession::getUsername)
                    .collect(Collectors.toList());
            return new MessageFormatter("Aktiivsed kasutaja ruumis: " + usernames.stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining(", ")), MessageFormatter.PURPLE);
        }

    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}