package Chatroom;

import Client.ClientSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomManager {
    private final Map<String, RegularChatRoom> rooms;

    public ChatRoomManager() {
        this.rooms = new HashMap<>();
    }

    /**
     * Kui kasutaja tahab ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
     * @param roomName
     * @return
     */
    public RegularChatRoom getOrCreateRoom(String roomName) {
        return rooms.computeIfAbsent(roomName, RegularChatRoom::new);
    }
    public List<String> listRoomNames() {
        return new ArrayList<>(rooms.keySet());
    }
    public void removeClientFromCurrentRoom(ClientSession session) {
        RegularChatRoom room = session.getCurrentRoom();
        if (room != null) {
            room.leave(session);
            session.setCurrentRoom(null);
        }
    }
}