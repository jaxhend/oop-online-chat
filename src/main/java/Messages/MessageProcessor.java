package Messages;

import Chatroom.ChatRoomManager;
import Messages.Commands.*;
import Server.*;

import java.util.List;

public class MessageProcessor {

    private final List<CommandHandler> handlers;


    public MessageProcessor(ChatRoomManager roomManager) {
        this.handlers = List.of(
                new JoinRoomCommand(roomManager),
                new PrivateJoinCommand(roomManager),
                new SeeMembersCommand(),
                new SeeChatroomsCommand(roomManager),
                new LeaveCommand(roomManager),
                new ChatMessageCommand() // tavasõnum
        );
    }

    public String processMessage(ClientSession session, String input) {
        // Kasutajanime määramine
        if (session.getUsername() == null) {
            return new UsernameCommand().handle(session, input);
        }

        for (CommandHandler handler : handlers) {
            if (handler.matches(input)) {
                return handler.handle(session, input);
            }
        }
        return null;
    }




}
