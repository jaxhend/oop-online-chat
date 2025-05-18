package com.online_chat.client;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientSessionManager {
    // Salvestame kõik aktiivsed sessioonid nende IDde järgi
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    // salvestame kõik cookied ja nendega seotud kasutajanimed
    private final Map<String, String> cookieUsernames = new ConcurrentHashMap<>();

    public ClientSessionManager() {
    }

    // Uue sessioni lisamine
    public void registerSession(ClientSession session) {
        sessions.put(session.getId(), session);
    }

    public ClientSession getSession(String id) {
        return sessions.get(id);
    }


    public List<String> getAllUsernames() {
        return sessions.values().stream()
                .map(ClientSession::getUsername)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    public List<ClientSession> getAllSessions() {
        return sessions.values().stream().toList();
    }

    public void setCookieUsername(String sessionId, String username) {
        cookieUsernames.put(sessionId, username);
    }

    public String getUsername(String sessionId) {
        return cookieUsernames.get(sessionId);
    }

    public void removeSession(String id) {
        sessions.remove(id);
    }
}