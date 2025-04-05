package Server;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;

import java.util.ArrayList;
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
        // Kontrollib, et kasutajanimi ei kattuks juba mõne aktiivse kasutajanimega.
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
            return handleGroupJoinCommand(session, input); // On "null", kui korrektne. Ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }
        if (input.startsWith(PRIVATE_JOIN_COMMAND)) {
            return handlePrivateJoinCommand(session, input); // On "null", kui korrektne. Ruumi sisenedes saadetakse sõnum broadcast() poolt.
        }

        // Aitab kuvada avalikud chatroomid
        if (input.startsWith(SEE_CHATROOMS_COMMAND)) {
            return handleSeeChatroomsCommand(session);
        }

        // Aitab kuvada chatroomis olevad liikmed
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

    // Kuvab aktiivsed kasutajad chatroomis ning serveris.
    private String handleSeeMembersCommand(ClientSession session) {
        if (session.getCurrentRoom() != null) {
            List<String> members = new ArrayList<>();
            session.getCurrentRoom().getParticipants().forEach(participant -> {
                members.add(participant.getUsername());
            });

            return "Aktiivsed kasutajad chatroomis " + session.getCurrentRoom().getName() + ": " + String.join(", ", members);
        }
        List<String> allUsers = ServerHandler.getAllUsernames();
        return String.join(", ", allUsers);
    }

    // Tagastab vastava teate ebasobiva kasutajanime kohta.
    private String handleInvalidUsername() {
        return "See kasutajanimi on juba võetud, palun vali uus." + "\n" + "Sisesta uus kasutajanimi: ";
    }

    // Kuvab avalikud chatroomid ning nendes olevate liikmete arvu.
    private String handleSeeChatroomsCommand(ClientSession session) {
        List<String> roomInfoList = new ArrayList<>();
        // Näita default chatroome esimesena (alati olemas)
        for (String name : roomManager.getDefaultRooms()) {
            ChatRoom room = roomManager.getOrCreateRoom(name, session, true);
            int participantCount = room.getParticipants().size();
            roomInfoList.add(name + "(" + participantCount + ")");
        }

        // Leiab ülejäänud chatroomid, kuhu saab liituda
        List<String> joinableRooms = roomManager.listJoinableRooms(session.getUsername());
        List<String> otherRooms = new ArrayList<>();
        for (String name : joinableRooms) {
            if(!roomManager.getDefaultRooms().contains(name)) {
                otherRooms.add(name);
            }
        }
        Collections.sort(otherRooms);
        for (String name : otherRooms) {
            ChatRoom room = roomManager.getOrCreateRoom(name, session, true);
            if (room != null) {
                roomInfoList.add(name + "(" + room.getParticipants().size() + ")");
            }
        }
        return "Hetkel avalikud chatroomid: " + String.join(", ", roomInfoList);
    }


    public String handleGroupJoinCommand(ClientSession session, String input) {
        String roomName = input.substring(GROUP_JOIN_COMMAND.length()).trim();

        if (roomName.isEmpty()) {
            return "Chatroomi nimi ei tohi olla tühi! Proovi uuesti.";

        }

        ChatRoom room = roomManager.getOrCreateRoom(roomName, session, true);

        roomManager.removeClientFromCurrentRoom(session);
        room.join(session); // Lisab ruumi ja saadab sõnumi.
        if (room.getParticipants().contains(session)) {
            session.setCurrentRoom(room);
        }
        return null;

    }

    public String handlePrivateJoinCommand(ClientSession session, String input) {
        String secondUser = input.substring(PRIVATE_JOIN_COMMAND.length()).trim();
        if (secondUser.isEmpty()) {
            return "Privaatsõnumi saaja nimi ei saa olla tühi! Proovi uuesti.";
        }
        List<String> allUsers = ServerHandler.getAllUsernames();
        if (allUsers.contains(secondUser)) {
            ChatRoom room = roomManager.getOrCreateRoom(secondUser, session, false);
            roomManager.removeClientFromCurrentRoom(session);
            room.join(session); // Liitub privaatvestlusega ja saadab sõnumi.
            session.setCurrentRoom(room);
        }

        return null;
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
