package com.online_chat.commands;


import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.service.MessageFormatter;

public class LeaveCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public LeaveCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        if (!inARoom(session))
            return new MessageFormatter("Sa ei ole üheski ruumis.", MessageFormatter.ERRORS);

        chatRoomManager.removeClientFromCurrentRoom(session);
        return new MessageFormatter("Lahkusid ruumist.", MessageFormatter.COMMANDS);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}