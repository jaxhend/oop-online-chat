package com.online_chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
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

    @Autowired
    public MessageProcessor(CommandHandler commandHandler, ClientSessionManager sessionManager) {
        this.commandHandler = commandHandler;
        this.sessionManager = sessionManager;
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
        if (sessionManager.inValidUserName(message)) {
            String error = "Kasutajanimi on keelatud või juba kasutusel. Proovi uuesti.";
            sendErrorMessage(session, error);
            return error;
        } else {
            session.setUsername(message);
            String welcome = String.format("Tere tulemast, %s! Kasuta /help, et näha käske.", message);
            sendMessage(session, welcome, "#2ECC71");
            return welcome;
        }
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