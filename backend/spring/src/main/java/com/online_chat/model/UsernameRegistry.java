package com.online_chat.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsernameRegistry {

    public static final int LOCK_DAYS = 30;
    private final Map<String, RegistryEntry> registeredNames = new ConcurrentHashMap<>();

    // Kontrollime, kas antud kasutajanimi on juba registreeritud. Kui ei ole, siis registreerime selle.
    // Üks lõim korraga.
    public synchronized boolean register(String username, String sessionId) {
        if (username == null || username.isBlank()) return false;

        RegistryEntry existing = registeredNames.get(username);

        if (existing != null) {
            if (existing.sessionId().equalsIgnoreCase(sessionId)) {
                return true;
            }

            if (existing.timestamp().plus(LOCK_DAYS, ChronoUnit.DAYS).isAfter(Instant.now())) {
                return false;
            }
        }

        registeredNames.put(username, new RegistryEntry(sessionId, Instant.now()));
        return true;
    }


    private record RegistryEntry(String sessionId, Instant timestamp) {
    }
}