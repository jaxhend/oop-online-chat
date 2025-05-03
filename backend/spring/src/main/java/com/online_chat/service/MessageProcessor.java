package com.online_chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.model.UsernameRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class MessageProcessor {

    private final CommandHandler commandHandler;
    private final ClientSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UsernameRegistry usernameRegistry;

    @Autowired
    public MessageProcessor(CommandHandler commandHandler, ClientSessionManager sessionManager, UsernameRegistry usernameRegistry) {
        this.commandHandler = commandHandler;
        this.sessionManager = sessionManager;
        this.usernameRegistry = usernameRegistry;
    }

    // Töötleb kasutaja saadetud sõnumi ning edastab selle
    public String processAndBroadcast(ClientSession session, String message) {
        if (session.getUsername() == null || session.getUsername().isBlank()) {
            return handleUsernameAssignment(session, message);
        } else if (message.startsWith("/")) {
            String response = commandHandler.handle(session, message);
            String color = message.startsWith("/join") ? "#9B59B6" : "#E74C3C";
            sendMessage(session, response, color);
            return response;
        } else if (session.getCurrentRoom() != null) {
            broadcastToRoom(session, message);
            return message;
        } else {
            String error = "Sa ei ole üheski toas. Kasuta /join <ruum>.";
            sendErrorMessage(session, error);
            return error;
        }
    }

    // kasutajanime töötlemine ja sobivuse kontroll
    private String handleUsernameAssignment(ClientSession session, String message) {
        String username = message.trim();

        if (username.isBlank() || username.contains("/") || username.contains(" ")) {
            sendErrorMessage(session, "Kasutajanimi on keelatud või vales formaadis.");
            return "Kasutajanimi ei sobi.";
        }


        if (sessionManager.isUserOnline(username)) {
            sendErrorMessage(session, "Kasutajanimi on juba kasutuses.");
            return "Kasutajanimi juba kasutuses.";
        }

        if (usernameRegistry.isTaken(username)) {
            sendErrorMessage(session, "See kasutajanimi on reserveeritud ja ei ole veel vaba.");
            return "Kasutajanimi lukus.";
        }

        usernameRegistry.register(username);

        session.setUsername(username);
        String welcome = String.format("Tere tulemast, %s! Kasuta /help, et näha käske.", username);
        sendMessage(session, welcome, "#2ECC71");
        return welcome;
    }

    // Edastab sõnumi kõigile kasutajatele, kes on saatjaga samas ruumis
    private void broadcastToRoom(ClientSession sender, String message) {
        long otherUsers = sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> !s.equals(sender))
                .count();

        if (otherUsers == 0) {
            sendMessage(sender, "Oled ruumis üksi.", "#FFA500");
            return;
        }

        String formatted = String.format("[%s] [%s] %s: %s",
                currentTime(),
                sender.getCurrentRoom().getName(),
                sender.getUsername(),
                message);

        String color = "#34495E";

        sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .map(ClientSession::getWebSocketSession)
                .filter(ws -> ws != null && ws.isOpen())
                .forEach(ws -> {
                    try {
                        ColoredMessage msg = new ColoredMessage(formatted, color);
                        String json = objectMapper.writeValueAsString(msg);
                        ws.sendMessage(new TextMessage(json));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void sendMessage(ClientSession session, String message, String color) {
        try {
            String formatted = "[" + currentTime() + "] " + message;
            ColoredMessage coloredMessage = new ColoredMessage(formatted, color);
            String json = objectMapper.writeValueAsString(coloredMessage);
            session.getWebSocketSession().sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(ClientSession session, String error) {
        try {
            String withTime = "[" + currentTime() + "] " + error;
            ColoredMessage errorMessage = new ColoredMessage(withTime, "red");
            String json = objectMapper.writeValueAsString(errorMessage);
            session.getWebSocketSession().sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}