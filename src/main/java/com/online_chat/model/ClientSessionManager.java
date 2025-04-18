package com.online_chat.model;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClientSessionManager {

    // Salvestame kõik aktiivsed sessioonid nende IDde järgi
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();

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

    // Kontrollime, kas kasutajanimi on juba kasutusel
    public boolean isUserOnline(String username) {
        return sessions.values().stream()
                .anyMatch(session -> username.equalsIgnoreCase(session.getUsername()));
    }

    // Kontrollime kasutajanime sobivust
    public boolean inValidUserName(String username) {
        return username == null ||
                username.isBlank() ||
                username.contains("/") ||
                username.contains(" ") ||
                isUserOnline(username);
    }
    public void removeSession(String id) {
        sessions.remove(id);
    }
}