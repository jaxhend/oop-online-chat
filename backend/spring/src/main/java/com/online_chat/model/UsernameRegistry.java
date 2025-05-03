package com.online_chat.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsernameRegistry {

    private final Map<String, RegistryEntry> registeredNames = new ConcurrentHashMap<>();
    private static final int LOCK_DAYS = 7;

    // kontrollime, kas antud kasutajanimi on juba registreeritud ja veel lukus.
    public synchronized boolean isTaken(String username, String sessionId) {
        RegistryEntry entry = registeredNames.get(username);
        if (entry == null) return false;

        if (entry.timestamp().plus(LOCK_DAYS, ChronoUnit.DAYS).isBefore(Instant.now())) {
            registeredNames.remove(username);
            return false;
        }

        return !entry.sessionId().equals(sessionId);
    }

    // uue kasutajanime registreerimine
    public synchronized boolean register(String username, String sessionId) {
        if (isTaken(username, sessionId)) return false;
        registeredNames.put(username, new RegistryEntry(sessionId, Instant.now()));
        return true;
    }

    private record RegistryEntry(String sessionId, Instant timestamp) {}
}