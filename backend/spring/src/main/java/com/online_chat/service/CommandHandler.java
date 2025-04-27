package com.online_chat.service;

import com.online_chat.commands.*;
import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandHandler {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler(ChatRoomManager chatRoomManager,
                          ClientSessionManager sessionManager) {
        commands.put("/username", new UsernameCommand(sessionManager));
        commands.put("/join", new JoinRoomCommand(chatRoomManager));
        commands.put("/leave", new LeaveCommand(chatRoomManager));
        commands.put("/chatrooms", new SeeChatroomsCommand(chatRoomManager));
        commands.put("/members", new SeeMembersCommand(sessionManager));
        commands.put("/private", new PrivateJoinCommand(chatRoomManager, sessionManager));
        commands.put("/msg", new ChatMessageCommand());
        commands.put("/help", new HelpCommand());
    }

    public String handle(ClientSession session, String input) {
        String[] parts = input.trim().split(" ");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "Tühi käsk.";
        }

        Command command = commands.get(parts[0].toLowerCase());
        if (command != null) {
            return command.execute(session, parts);
        }

        return "Tundmatu käsk: " + parts[0] + ". Kasuta /help, et näha käske.";
    }
}