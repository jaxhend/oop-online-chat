package CLI;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Client.ClientSession;
import Server.ServerHandler;
import io.netty.channel.Channel;

import java.util.Collections;
import java.util.List;

import static Server.ServerHandler.sessions;

public class MessageProcessor {
    private final ChatRoomManager roomManager;
    private static final String GROUP_JOIN_COMMAND = "/room ";
    private static final String PRIVATE_JOIN_COMMAND = "/direct ";
    private static final String SEE_MEMBERS_COMMAND = "/members";
    private static final String SEE_CHATROOMS_COMMAND = "/chatrooms";
    private static final String LEAVE_COMMAND = "/leave";

    public MessageProcessor(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public String processMessage(ClientSession session, String input) {
        // Kasutajanime määramine
        // TODO: Kasutajanimed ei tohi kattuda
        if (session.getUsername() == null) {
            String username = input.trim();
            for (ClientSession user : sessions.values()) {
                if (username.equalsIgnoreCase(user.getUsername())) {
                    return handleInvalidUsername();

                }
            }
            session.setUsername(username);
            return null;
        }

        // Chatroomiga liitumine
        if (input.startsWith(GROUP_JOIN_COMMAND)) {
            handleGroupJoinCommand(session, input);
            return null; // On null, kuna ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }
        if (input.startsWith(PRIVATE_JOIN_COMMAND)) {
            handlePrivateJoinCommand(session, input);
            return null; // On null, kuna ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }

        if (input.startsWith(SEE_CHATROOMS_COMMAND)) {
            return handleSeeChatroomsCommand();
        }

        if (input.startsWith(SEE_MEMBERS_COMMAND)) {
            return handleSeeMembersCommand(session);
        }

        // Chatroomist lahkumine
        if (input.startsWith(LEAVE_COMMAND)) {
            return handleLeaveCommand(session);
        }

        // Tavalise sõnumi saatmine
        return handleChatMessage(session, input);
    }

    private String handleSeeMembersCommand(ClientSession session) {
        if (session.getCurrentRoom() != null) {
            return "Aktiivseid kasutajaid chatroomis: " + session.getCurrentRoom().getName() + session.getCurrentRoom().activeMembers();
        }
        return "Aktiivsed kasutajaid: " + sessions.size();
    }

    private String handleInvalidUsername() {
        return "See kasutajanimi on juba võetud, palun vali uus." + "\n" + "Sisesta uus kasutajanimi: ";
    }

    private String handleSeeChatroomsCommand() {

        return "Hetkel avalikud grupid: " + roomManager.listRoomNames();
    }


    public void handleGroupJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(GROUP_JOIN_COMMAND.length()).trim();
        //ChatRoom room = roomManager.getOrCreateRoom(roomName, session, true);
        ChatRoom room = roomManager.getOrCreateRoom(roomName, session, true);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Lisab ruumi ja saadab sõnumi.
        session.setCurrentRoom(room);
    }
    public void handlePrivateJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(PRIVATE_JOIN_COMMAND.length()).trim();
        //ChatRoom room = roomManager.getOrCreateRoom(roomName, session, false);
        ChatRoom room = roomManager.getOrCreateRoom(roomName, session, false);
        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Liitub privaatvestlusega ja saadab sõnumi.
        session.setCurrentRoom(room);
    }


    public String handleLeaveCommand(ClientSession session) {
        roomManager.removeClientFromCurrentRoom(session); // Saadab teistele ruumis olijatele sõnumi.
        return "Lahkusid ruumist."; // Kasutaja saab sõnumi, et on ruumist lahkunud.
    }

    public String handleChatMessage(ClientSession session, String message) {
        ChatRoom room = session.getCurrentRoom();
        if (room == null && !message.startsWith(SEE_CHATROOMS_COMMAND)) {
            return "Te pole üheski chatruumis, kasuta " + GROUP_JOIN_COMMAND + " + chatruumi nimi, et liituda grupiga" +
                    "\n" + "või kasuta " + PRIVATE_JOIN_COMMAND + " + kasutaja nimi, et saata privaatsõnum";
        }

        room.broadcast(message, session, true);
        return null;
    }
}

