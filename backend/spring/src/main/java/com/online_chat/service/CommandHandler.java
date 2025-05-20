package com.online_chat.service;

import com.online_chat.commands.*;
import com.online_chat.chatrooms.ChatRoomManager;
import com.online_chat.client.ClientSession;
import com.online_chat.client.ClientSessionManager;
import com.online_chat.model.MessageFormatter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandHandler {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler(ChatRoomManager chatRoomManager, ClientSessionManager sessionManager) {
        commands.put("/liitu", new JoinRoomCommand(chatRoomManager));
        commands.put("/lahku", new LeaveCommand(chatRoomManager));
        commands.put("/ruumid", new SeeChatroomsCommand(chatRoomManager));
        commands.put("/kasutajad", new ListMembersCommand(sessionManager));
        commands.put("/privaat", new PrivateJoinCommand(chatRoomManager, sessionManager));
        commands.put("/abi", new HelpCommand());
    }

    public MessageFormatter handle(ClientSession session, String input) {
        String[] parts = input.split(" ");

        Command command = commands.get(parts[0].toLowerCase());
        if (command != null) {
            return command.execute(session, parts);
        }

        return new MessageFormatter("Tundmatu käsk: " + parts[0] + ". Kasuta /abi, et näha käske.", MessageFormatter.RED);
    }
}