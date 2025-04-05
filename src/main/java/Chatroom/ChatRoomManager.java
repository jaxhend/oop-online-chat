package Chatroom;

import Server.ClientSession;

import java.util.*;

public class ChatRoomManager {
    private final Map<String, ChatRoom> rooms;
    private List<String> defaultRooms = List.of("oop", "proge", "varia");

    public ChatRoomManager() {
        this.rooms = new HashMap<>();
        // 3 default chatruumi, mis on alati olemas.
        for (String roomName : defaultRooms) {
            this.rooms.put(roomName, new RegularChatRoom(roomName));
        }
    }

    // Kui kasutaja tahab ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
    public ChatRoom getOrCreateRoom(String roomName, ClientSession session, boolean isPublic) {

        if (isPublic) {
            return rooms.computeIfAbsent(roomName, RegularChatRoom::new);
        }
        // Kui kasutaja tahab kellelegi privaatselt kirjutada
        List<String> users = Arrays.asList(session.getUsername().toUpperCase(), roomName.toUpperCase());
        // Kasutame Collection.sort-i, et mõlemad kasutajad saaksid liituda chatiga kasutades
        // käsu parameetrina teise inimese nime
        Collections.sort(users);
        String privateRoomName = users.get(0) + ":" + users.get(1);
        return rooms.computeIfAbsent(privateRoomName, key -> new PrivateChatRoom(
                key,
                users.get(0),
                users.get(1)
        ));
    }

    public List<String> getDefaultRooms() {
        return defaultRooms;
    }

    public List<String> listJoinableRooms(String username) {
        List<String> joinableRooms = new ArrayList<>();
        for (Map.Entry<String, ChatRoom> entry : rooms.entrySet()) {
            ChatRoom room = entry.getValue();
            if (room.canJoin(username)) {
                joinableRooms.add(room.getName());
            }
        }
        return joinableRooms;
    }

    public void removeClientFromCurrentRoom(ClientSession session) {
        ChatRoom room = session.getCurrentRoom();
        if (room != null) {
            room.leave(session);
            session.setCurrentRoom(null);

            // Kustutab chatruumi, kui seal pole ühtegi liiget ega see ei ole 3 algselt loodud chatruumi.
            if (room.activeMembers() == 0 & !defaultRooms.contains(room.getName()))
                rooms.remove(room.getName());
        }
    }
}