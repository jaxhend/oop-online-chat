package Server;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;

public class MessageProcessor {
    private final ChatRoomManager roomManager;
    private static final String JOIN_COMMAND = "/join ";
    private static final String LEAVE_COMMAND = "/exit";


    public MessageProcessor(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public String processMessage(ClientSession session, String input) {
        // Kasutajanime määramine
        if (session.getUsername() == null) {
            String username = input.trim();
            session.setUsername(username);
            return null;
        }

        // Chatroomiga liitumine
        if (input.startsWith(JOIN_COMMAND)) {
            handleJoinCommand(session, input);
            return null; // On null, kuna ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }

        // Chatroomist lahkumine
        if (input.startsWith(LEAVE_COMMAND)) {
            return handleLeaveCommand(session);
        }

        // Tavalise sõnumi saatmine
        return handleChatMessage(session, input);
    }


    public void handleJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(JOIN_COMMAND.length()).trim();
        ChatRoom room = roomManager.getOrCreateRoom(roomName);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Lisab ruumi ja saadab sõnumi.
        session.setCurrentRoom(room);
    }


    public String handleLeaveCommand(ClientSession session) {
        roomManager.removeClientFromCurrentRoom(session); // Saadab teistele ruumis olijatele sõnumi.
        return "Lahkusid ruumist."; // Kasutaja saab sõnumi, et on ruumist lahkunud.
    }

    public String handleChatMessage(ClientSession session, String message) {
        ChatRoom room = session.getCurrentRoom();
        if (room == null) {
            return "Te pole ühegi chatruumi või privaatsõnumiga ühinenud, kasuta /help abi saamiseks";
        }

        room.broadcast(message, session, true);
        return null;
    }
}
