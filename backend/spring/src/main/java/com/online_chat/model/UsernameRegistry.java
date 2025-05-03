package com.online_chat.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UsernameRegistry {

    private final Map<String, Instant> registeredNames = new ConcurrentHashMap<>();
    private static final int LOCK_DAYS = 7;

    // kontrollime, kas antud kasutajanimi on juba registreeritud ja veel lukus.
    public synchronized boolean isTaken(String username) {
        Instant registeredAt = registeredNames.get(username);
        if (registeredAt == null) return false;

        if (registeredAt.plus(LOCK_DAYS, ChronoUnit.DAYS).isBefore(Instant.now())) {
            registeredNames.remove(username);
            return false;
        }
        return true;
    }

    // uue kasutajanime registreerimine
    public synchronized boolean register(String username) {
        if (isTaken(username)) return false;
        registeredNames.put(username, Instant.now());
        return true;
    }
}