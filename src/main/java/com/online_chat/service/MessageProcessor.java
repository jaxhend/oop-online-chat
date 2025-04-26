package com.online_chat.service;


import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class  MessageProcessor {

    private final CommandHandler commandHandler;
    private final ClientSessionManager sessionManager;

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
            sendMessage(session, response);
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
            sendMessage(session, welcome);
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
            sendMessage(sender, "Oled ruumis üksi.");
            return;
        }
        String formatted = String.format("[%s] [%s] %s: %s",
                currentTime(),
                sender.getCurrentRoom().getName(),
                sender.getUsername(),
                message);
        // Leiame kõik kasutajad, kes on samas ruumis
        sessionManager.getAllSessions().stream()
                .filter(s -> // leiame kõik, kes on kasutajaga samas chatroomis
                        sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .map(ClientSession::getWebSocketSession) // kõikide kasutajate WebSocketSessionid
                .filter(ws -> ws != null && ws.isOpen())
                .forEach(ws -> { // saadame kõigile WebSocketSessionidele sõnumid
                    try {
                        ws.sendMessage(new TextMessage(formatted));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void sendMessage(ClientSession session, String message) {
        try {
            String formatted = "[" + currentTime() + "] " + message;
            session.getWebSocketSession().sendMessage(new TextMessage(formatted));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(ClientSession session, String error) {
        try {
            String withTime = String.format("[%s] %s", currentTime(), error);
            session.getWebSocketSession().sendMessage(new TextMessage(withTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}