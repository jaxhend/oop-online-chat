package Messages.Commands;

import Chatroom.ChatRoom;
import Server.ClientSession;

import static Messages.Commands.JoinRoomCommand.ROOM_JOIN_COMMAND;
import static Messages.Commands.PrivateJoinCommand.PRIVATE_ROOM_COMMAND;
import static Messages.Commands.SeeChatroomsCommand.SEE_CHATROOMS_COMMAND;


public class ChatMessageCommand implements CommandHandler {
    @Override
    public boolean matches(String message) {
        return true;
    }

    @Override
    public String handle(ClientSession session, String message) {
        ChatRoom room = session.getCurrentRoom();

        if (room == null && !message.startsWith(SEE_CHATROOMS_COMMAND)) {
            return "Te pole üheski chatruumis, kasuta " + ROOM_JOIN_COMMAND + " + chatruumi nimi, et liituda grupiga" +
                    "\n" + "või kasuta " + PRIVATE_ROOM_COMMAND + " + kasutaja nimi, et saata privaatsõnum";
        }
        room.broadcast(message, session, true);
        return null;
    }
}
