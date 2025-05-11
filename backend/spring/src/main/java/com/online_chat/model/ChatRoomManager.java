package com.online_chat.model;

import com.online_chat.service.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.online_chat.service.MessageProcessor.objectMapper;

@Component
public class ChatRoomManager {
    // 3 default chatruumi, mis on alati olemas.
    public static final List<String> defaultRooms = List.of("proge", "majandus", "varia");
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomManager.class);
    private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> roomTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public ChatRoomManager() {
        for (String roomName : defaultRooms) {
            rooms.put(roomName, new RegularChatRoom(roomName));
        }

    }

    public ChatRoom getRoom(String roomName) {
        return rooms.get(roomName);
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
            if (!room.getClients().isEmpty()) {
                // saadame kõigile teistele ruumis sõnumi, et kasutaja liitus ruumiga
                for (ClientSession member : room.getClients()) {
                    if (member.getWebSocketSession() != null && member.getWebSocketSession().isOpen()) {
                        try {
                            String text;
                            if (room.isPublicChatRoom())
                                text = String.format("Kasutaja '%s' liitus ruumiga '%s'.", client.getUsername(), roomName);
                            else
                                text = String.format("Kasutaja '%s' liitus privaatvestlusega.", client.getUsername());

                            String json = objectMapper.writeValueAsString(new MessageFormatter(text, MessageFormatter.ORANGE));
                            member.getWebSocketSession().sendMessage(new TextMessage(json));
                        } catch (IOException e) {
                            logger.error("Ruumi liikmetele teise kasutaja liitumissõnumi saatmise error. ", e);
                        }
                    }
                }
            }
            room.join(client); // lõpuks lisame kasutaja ruumi
            startRoomTimer(room);
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
                            String text = "Kasutaja '" + client.getUsername() + "' lahkus ruumist '" + room.getName() + "'.";
                            String json = objectMapper.writeValueAsString(new MessageFormatter(text, MessageFormatter.ORANGE));
                            ws.sendMessage(new TextMessage(json));
                        } catch (IOException e) {
                            logger.error("Ruumi liikmetele teise kasutaja lahkumissõnumi saatmise error. ", e);
                        }
                    });
            room.leave(client);

            checkIfNeedsToRemove(room); // Alustab 24h timer.
        }
    }


    // Kustutab chatruumi, kui seal pole ühtegi liiget ning see ei ole 3 algselt loodud chatruumi.
    private void checkIfNeedsToRemove(ChatRoom room) {
        int clientCount = room.getClientsCount();

        if (clientCount == 0 && room instanceof PrivateChatRoom)
            rooms.remove(room.getName()); // Privaatvestlused kustutatakse kohe
        else if (clientCount == 0 && !defaultRooms.contains(room.getName())) {
            startRoomTimer(room); // Ruumid kustutatakse 24h pärast, kui keegi ei liitu
        }
    }

    private void startRoomTimer(ChatRoom room) {
        if (roomTimers.containsKey(room.getName())) {
            roomTimers.get(room.getName()).cancel(false); // Tühistab eelmise timeri
        }

        // Ajastage ruumi eemaldamine 24 tunni pärast, kui keegi ei liitu
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> rooms.remove(room.getName()), 24, TimeUnit.HOURS);
        roomTimers.put(room.getName(), scheduledFuture);
    }

    public Map<String, ChatRoom> getRoomInfo() {
        return this.rooms;
    }

}