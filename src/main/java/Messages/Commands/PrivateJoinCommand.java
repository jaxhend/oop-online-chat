package Messages.Commands;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Server.ClientSession;
import Server.ServerHandler;

import java.util.List;

public class PrivateJoinCommand implements CommandHandler {
    public static final String PRIVATE_ROOM_COMMAND = "/direct ";
    private final ChatRoomManager roomManager;

    public PrivateJoinCommand(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith("/direct ");
    }

    @Override
    public String handle(ClientSession session, String input) {
        String secondUser = input.substring(PRIVATE_ROOM_COMMAND.length()).trim();
        List<String> allUsers = ServerHandler.getAllUsernames();

        if (secondUser.isEmpty()) {
            return "Privaatsõnumi saaja nimi ei saa olla tühi! Proovi uuesti.";
        } else if (secondUser.equalsIgnoreCase(session.getUsername())) {
            return "Privaatsõnumit ei saa endale saata!";
        } else if (allUsers.stream().anyMatch(user -> user.equalsIgnoreCase(secondUser))) {
            ChatRoom room = roomManager.getOrCreateRoom(secondUser, session, false);
            roomManager.removeClientFromCurrentRoom(session);
            room.join(session); // Liitub privaatvestlusega ja saadab sõnumi.
            session.setCurrentRoom(room);
        } else {
            return "Privaatsõnumi saaja peab olema aktiivne kasutaja";
        }
        return null;
    }
}
