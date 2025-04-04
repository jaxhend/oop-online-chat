package CLI;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Chatroom.RegularChatRoom;
import Client.ClientSession;

public class MessageProcessor {
    private final ChatRoomManager roomManager;
    private static final String JOIN_COMMAND = "/join ";
    private static final String LEAVE_COMMAND = "/leave";

    public MessageProcessor(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public String processMessage(ClientSession session, String input) {
        // Kui kasutaja pole sisetanud enda nime siis küsitakse seda temalt
        if (session.getUsername() == null) {
            return handleUsernameRegistration(session, input);
        }


        // Chatroomiga liitumine

        if (input.startsWith(JOIN_COMMAND)) {
            handleJoinCommand(session, input);
            return null;
        }



        // Chatroomist lahkumine
        if (input.startsWith(LEAVE_COMMAND)) {
            return handleLeaveCommand(session);
        }

        // tavalise sõnumi saatmine
        return handleChatMessage(session, input);
    }

    private String handleUsernameRegistration(ClientSession session, String input) {
        String username = input.trim();
        session.setUsername(username);
        return "Kasuta " + JOIN_COMMAND + ", et liituda chatroomiga";
    }


    private void handleJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(JOIN_COMMAND.length()).trim();
        RegularChatRoom room = roomManager.getOrCreateRoom(roomName);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session);
        session.setCurrentRoom(room);
    }



    private String handleLeaveCommand(ClientSession session) {
        roomManager.removeClientFromCurrentRoom(session);
        return "Lahkusid ruumist.";
    }

    private String handleChatMessage(ClientSession session, String message) {
        ChatRoom room = session.getCurrentRoom();
        if (room == null) {
            return "Te pole üheski chatruumis, kasuta " + JOIN_COMMAND + " + chatruumi_nimi, et liituda chatruumiga";
        }

        room.broadcast(session.getUsername() + ": " + message);
        return null;
    }
}
