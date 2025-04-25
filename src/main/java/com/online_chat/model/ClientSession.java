package com.online_chat.model;

import org.springframework.web.socket.WebSocketSession;

public class ClientSession {
    // Igal sessionil on unikaalne id
    private final String id;
    // Clientsessionit luues puudub username, lisame selle hiljem setUsername-ga
    private String username = "";
    private ChatRoom currentRoom;
    private transient WebSocketSession webSocketSession;

    public ClientSession(String id) {
        this.id = id;
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
