package Chatroom;

import Client.ClientSession;

import java.util.*;

public class ChatRoomManager {
    private final Map<String, ChatRoom> rooms;
    private List<String> defaulRooms = List.of("oop", "proge", "varia");

    public ChatRoomManager() {
        this.rooms = new HashMap<>();
        // 3 default chatruumi, mis on alati olemas.
        for (String roomName : defaulRooms) {
            this.rooms.put(roomName, new RegularChatRoom(roomName));
        }
    }

    // Kui kasutaja tahab ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
    public ChatRoom getOrCreateRoom(String roomName, ClientSession session, boolean isPublic) {
        // Kui kasutaja tahab liituda avalikuruumiga
        if (isPublic) {
            return rooms.computeIfAbsent(roomName, RegularChatRoom::new);
        }
        // Kui kasutaja tahab kellelegi privaatselt kirjutada
        List<String> sortedUsers = Arrays.asList(session.getUsername(), roomName);
        // Kasutame Collection.sort-i, et mõlemad kasutajad saaksid liituda chatiga kasutades
        // käsu parameetrina teise inimese nimi
        Collections.sort(sortedUsers);
        String privateRoomName = sortedUsers.get(0) + ":" + sortedUsers.get(1);
        return rooms.computeIfAbsent(privateRoomName, PrivateChatRoom::new);
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
            if (room.activeMembers() == 0 & !defaulRooms.contains(room.getName()))
                rooms.remove(room);
        }
    }
}