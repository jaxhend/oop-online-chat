package com.online_chat.commands;


import com.online_chat.model.ClientSession;
import com.online_chat.service.ColoredMessage;

public interface Command {
    ColoredMessage execute(ClientSession session, String[] args);

    boolean validCommand(String[] args);

    default boolean inARoom(ClientSession session) {
        return session.getCurrentRoom() != null;
    }

}