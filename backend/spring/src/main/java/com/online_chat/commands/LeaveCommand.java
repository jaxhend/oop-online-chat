package com.online_chat.commands;


import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.service.ColoredMessage;

public class LeaveCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public LeaveCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public ColoredMessage execute(ClientSession session, String[] args) {
        if (!inARoom(session))
            return new ColoredMessage("Sa ei ole üheski ruumis.", ColoredMessage.ERRORS);

        chatRoomManager.removeClientFromCurrentRoom(session);
        return new ColoredMessage("Lahkusid ruumist.", ColoredMessage.COMMANDS);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}