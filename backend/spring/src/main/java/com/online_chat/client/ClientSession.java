package com.online_chat.client;

import com.online_chat.chatrooms.ChatRoom;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ClientSession {
    // Igal sessionil on unikaalne id
    private final String id;
    // Clientsessionit luues puudub username, lisame selle hiljem setUsername-ga
    private String username = "";
    private ChatRoom currentRoom;
    private transient WebSocketSession webSocketSession;
    private Map<String, LocalDateTime> lastSeenMessages;

    public ClientSession(String id) {
        this.id = id;
        this.lastSeenMessages = new HashMap<>();
    }


    public LocalDateTime getLastSeenTimestamp(String chatRoom) {
        return lastSeenMessages.get(chatRoom);
    }

    public void updateLastSeenMessage(String chatRoom) {
        lastSeenMessages.put(chatRoom, LocalDateTime.now());
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }
}
