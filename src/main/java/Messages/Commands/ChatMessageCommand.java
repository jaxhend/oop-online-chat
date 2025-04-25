package Messages.Commands;

import Chatroom.ChatRoom;
import Server.ClientSession;

import java.util.List;
import java.util.stream.Collectors;

import static Messages.Commands.JoinRoomCommand.ROOM_JOIN_COMMAND;
import static Messages.Commands.NewsCommand.NEWS_COMMAND;
import static Messages.Commands.PrivateJoinCommand.PRIVATE_ROOM_COMMAND;
import static Messages.Commands.SeeChatroomsCommand.SEE_CHATROOMS_COMMAND;


public class ChatMessageCommand implements CommandHandler {
    @Override
    public boolean matches(String input) {
        return true;
    }

    @Override
    public String handle(ClientSession session, String message) {
        List<String> byPassingCommands = List.of(SEE_CHATROOMS_COMMAND, NEWS_COMMAND);
        ChatRoom room = session.getCurrentRoom();

        boolean isByPassingCommand = byPassingCommands.stream().anyMatch(message::startsWith);

        if (room == null && !isByPassingCommand) {
            return "Te pole üheski chatruumis, kasuta " + ROOM_JOIN_COMMAND + " + chatruumi nimi, et liituda grupiga" +
                    "\n" + "või kasuta " + PRIVATE_ROOM_COMMAND + " + kasutaja nimi, et saata privaatsõnum";
        }
        room.broadcast(message, session, true);
        return null;
    }
}
