package com.online_chat.model;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomManager {
    // 3 default chatruumi, mis on alati olemas.
    public static final List<String> defaultRooms = List.of("proge", "majandus", "varia");
    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    public ChatRoomManager() {
        for (String roomName : defaultRooms) {
            rooms.put(roomName, new RegularChatRoom(roomName));
        }
    }

    public ChatRoom getRoom(String name) {
        return rooms.get(name);
    }

    public boolean roomExists(String roomName) {
        return rooms.containsKey(roomName);
    }

    // Kui kasutaja tahab avaliku ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
    public void getOrCreatePublicRoom(String roomName) {
        rooms.computeIfAbsent(roomName, RegularChatRoom::new);
    }

    // Kui kasutaja tahab privaatse ruumiga liituda, aga seda ei eksisteeri, siis luuakse ruum.
    public void getOrCreatePrivateRoom(String roomId, ClientSession owner, ClientSession invited) {
        rooms.computeIfAbsent(roomId, id -> new PrivateChatRoom(id, owner, invited));
    }

    public void addClientToRoom(ClientSession client, String roomName) {
        // leiame ruumi, millega kasutaja tahab liituda
        ChatRoom room = rooms.get(roomName);
        // kontrollime, kas kasutaja saab tohib ruumiga liituda
        if (room != null && room.canJoin(client)) {
            // kontrollime, kas on kellelgi vaja teavitus saata
            if (room.getClients().size() != 0) {
                // saadame kõigile teistele ruumis sõnumi, et kasutaja liitus ruumiga
                for (ClientSession member : room.getClients()) {
                    if (member.getWebSocketSession() != null && member.getWebSocketSession().isOpen()) {
                        try {
                            String message;
                            if (room.isPublicChatRoom()) {
                                message = String.format("Kasutaja '%s' liitus ruumiga '%s'.", client.getUsername(), roomName);
                            } else {
                                message = String.format("Kasutaja '%s' liitus privaatvestlusega.", client.getUsername());
                            }

                            member.getWebSocketSession().sendMessage(new TextMessage(message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            room.join(client); // lõpuks lisame kasutaja ruumi
        }
    }

    public void removeClientFromCurrentRoom(ClientSession client) {
        // leiame ruumi, kus kasutaja on
        ChatRoom room = client.getCurrentRoom();
        // kui kasutaja on ruumis, siis saadame teistele teavituse ja eemaldame kasutaja ruumist
        if (room != null) {
            room.getClients().stream()
                    .filter(member -> !member.equals(client))
                    .map(ClientSession::getWebSocketSession)
                    .filter(ws -> ws != null && ws.isOpen())
                    .forEach(ws -> {
                        try {
                            ws.sendMessage(new TextMessage("Kasutaja '" + client.getUsername() + "' lahkus ruumist '" + room.getName() + "'."));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            room.leave(client);

            checkIfNeedsToRemove(room);
        }
    }

    // Kustutab chatruumi, kui seal pole ühtegi liiget ega see ei ole 3 algselt loodud chatruumi.
    public void checkIfNeedsToRemove(ChatRoom room) {
        if (room.getClientsCount() == 0 && !defaultRooms.contains(room.getName())) {
            rooms.remove(room.getName());
        }
    }

    public Set<String> getRoomNames() {
        return rooms.keySet();
    }


}