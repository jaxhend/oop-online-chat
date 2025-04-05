package CLI;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Client.ClientSession;

public class MessageProcessor {
    private final ChatRoomManager roomManager;
    private static final String GROUP_JOIN_COMMAND = "/join Group ";
    private static final String PRIVATE_JOIN_COMMAND = "/join DM ";
    private static final String SEE_MEMBERS_COMMAND = "/members";
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
        if (input.startsWith(GROUP_JOIN_COMMAND)) {
            handleGroupJoinCommand(session, input);
            return null; // On null, kuna ruumi sisenedes saadetakse sõnum broadcast() poolt.
        } else if (input.startsWith(PRIVATE_JOIN_COMMAND)) {
            handlePrivateJoinCommand(session, input);
            return null; // On null, kuna ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }

        if (input.startsWith(SEE_MEMBERS_COMMAND)) {
            System.out.println(roomManager.listRoomNames());
        }

        // Chatroomist lahkumine
        if (input.startsWith(LEAVE_COMMAND)) {
            return handleLeaveCommand(session);
        }

        // Tavalise sõnumi saatmine
        return handleChatMessage(session, input);
    }


    public void handleGroupJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(GROUP_JOIN_COMMAND.length()).trim();
        ChatRoom room = roomManager.getOrCreateRoom(roomName, session, true);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Lisab ruumi ja saadab sõnumi.
        session.setCurrentRoom(room);
    }
    public void handlePrivateJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(PRIVATE_JOIN_COMMAND.length()).trim();
        ChatRoom room = roomManager.getOrCreateRoom(roomName,session, false);
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
            return "Te pole üheski chatruumis, kasuta " + GROUP_JOIN_COMMAND + " + chatruumi nimi, et liituda grupiga" +
                    "\n" + "või kasuta " + PRIVATE_JOIN_COMMAND + " + kasutaja nimi, et saata privaatsõnum";
        }


        room.broadcast(message, session, true);
        return null;
    }
}
