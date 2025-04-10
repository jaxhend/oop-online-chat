package Messages.Commands;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Server.ClientSession;

public class JoinRoomCommand implements CommandHandler {
    public static final String ROOM_JOIN_COMMAND = "/room ";
    private final ChatRoomManager roomManager;

    public JoinRoomCommand(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith("/room ");
    }

    @Override
    public String handle(ClientSession session, String input) {
        String roomName = input.substring(ROOM_JOIN_COMMAND.length()).trim();
        if (roomName.isEmpty()) {
            return "Chatroomi nimi ei tohi olla tühi! Proovi uuesti.";
        }

        if (roomName.contains(":")) {
            return "Keelatud chatroomi nimi, vali uuesti.";
        }

        ChatRoom room = roomManager.getOrCreateRoom(roomName, session, true);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Lisab ruumi ja saadab sõnumi.
        if (room.getParticipants().contains(session)) {
            session.setCurrentRoom(room);
        }
        return null;
    }
}
