package Chatroom;

import Client.ClientSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomManager {
    private final Map<String, RegularChatRoom> rooms;
    private List<String> defaultRooms = List.of("oop", "proge", "varia");

    public ChatRoomManager() {
        this.rooms = new HashMap<>();
        // 3 default chatruumi, mis on alati olemas.
        for (String roomName : defaultRooms) {
            this.rooms.put(roomName, new RegularChatRoom(roomName));
        }
    }

    // Kui kasutaja tahab ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
    public ChatRoom getOrCreateRoom(String roomName) {
        ChatRoom room = rooms.computeIfAbsent(roomName, RegularChatRoom::new);
        room.setPublic(true);
        return rooms.computeIfAbsent(roomName, RegularChatRoom::new);
    }


    public List<String> listRoomNames() {
        return new ArrayList<>(rooms.keySet());
    }


    public void removeClientFromCurrentRoom(ClientSession session) {
        ChatRoom room = session.getCurrentRoom();
        if (room != null) {
            room.leave(session);
            session.setCurrentRoom(null);

            // Kustutab chatruumi, kui seal pole ühtegi liiget ega see ei ole 3 algselt loodud chatruumi.
            if (room.activeMembers() == 0 && !defaultRooms.contains(room.getName()))
                rooms.remove(room.getName());
        }
    }
}