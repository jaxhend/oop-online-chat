package Messages.Commands;

import Chatroom.ChatRoom;
import Chatroom.ChatRoomManager;
import Server.ClientSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeeChatroomsCommand implements CommandHandler {
    public static final String SEE_CHATROOMS_COMMAND = "/chatrooms";
    private final ChatRoomManager roomManager;

    public SeeChatroomsCommand(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith(SEE_CHATROOMS_COMMAND);
    }


    @Override
    public String handle(ClientSession session, String input) {
        List<String> roomInfoList = new ArrayList<>();
        // Näita default chatroome esimesena (alati olemas)
        for (String name : roomManager.defaultRooms) {
            ChatRoom room = roomManager.getOrCreateRoom(name, session, true);
            int participantCount = room.getParticipants().size();
            roomInfoList.add(name + "(" + participantCount + ")");
        }

        // Leiab ülejäänud chatroomid, kuhu saab liituda
        List<String> joinableRooms = roomManager.listJoinableRooms(session.getUsername());
        List<String> otherRooms = new ArrayList<>();
        for (String name : joinableRooms) {
            if (!roomManager.defaultRooms.contains(name)) {
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

    public String getCommand() {
        return SEE_CHATROOMS_COMMAND;
    }
}
