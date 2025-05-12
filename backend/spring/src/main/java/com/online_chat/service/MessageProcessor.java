package com.online_chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_chat.bots.weatherBot.WeatherApi;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.model.UsernameRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import static com.online_chat.service.MessageFormatter.WET_ASPHALT;

@Service
public class MessageProcessor {

    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(WeatherApi.class);
    private final CommandHandler commandHandler;
    private final ClientSessionManager sessionManager;
    private final UsernameRegistry usernameRegistry;


    @Autowired
    public MessageProcessor(CommandHandler commandHandler, ClientSessionManager sessionManager, UsernameRegistry usernameRegistry) {
        this.commandHandler = commandHandler;
        this.sessionManager = sessionManager;
        this.usernameRegistry = usernameRegistry;
    }

    // Töötleb kasutaja saadetud sõnumi ning edastab selle
    public void processAndBroadcast(ClientSession session, String message) {
        int MAX_MESSAGE_LENGTH = 500;
        if (session.getUsername() == null || session.getUsername().isBlank()) {
            handleUsernameAssignment(session, message);

        } else if (message.length() > MAX_MESSAGE_LENGTH) {
            sendMessage(session, new MessageFormatter("Sõnum on liiga pikk. Proovi uuesti!", MessageFormatter.ERRORS));

        } else if (message.startsWith("/")) {
            MessageFormatter response = commandHandler.handle(session, message);
            sendMessage(session, response);

        } else if (session.getCurrentRoom() != null) { // Tavasõnumi väljasaatmine.
            String msg = message.replace("\n", " ")
                    .replace("\r", " ")
                    .replaceAll("\\s+", " ");
            broadcastToRoom(session, msg);
        } else {
            String error = """
                    Sa ei ole üheski vestlusruumis. Kasuta käske:
                    /join <chatruumi_nimi>, et liituda või luua vestlusruumiga,
                    /private <kasutaja_nimi>, et alustada privaatsõnumit.""";
            sendMessage(session, new MessageFormatter(error, MessageFormatter.ERRORS));
        }
    }

    // kasutajanime töötlemine ja sobivuse kontroll
    public void handleUsernameAssignment(ClientSession session, String message) {
        String username = message.trim();

        if (username.contains("/"))
            sendUsernameMessage(session, "Kasutajanimi ei tohi '/' sisaldada.");
        else if (username.length() <= 3)
            sendUsernameMessage(session, "Kasutajanimi peab olema vähemalt 4 tähemärki.");
        else if (username.length() > 30)
            sendUsernameMessage(session, "Kasutajanimi peab olema vähem kui 30 tähemärki.");
        else if (username.contains("-"))
            sendUsernameMessage(session, "Kasutajanimi ei tohi '-' sisaldada.");
        else if (username.contains(" "))
            sendUsernameMessage(session, "Kasutajanimi ei tohi tühikut sisaldada.");
        else if (username.isBlank())
            sendUsernameMessage(session, "Kasutajanimi ei saa tühi olla.");

        else if (!usernameRegistry.register(username, session.getId()))
            sendUsernameMessage(session, "Kasutajanimi on juba võetud.");
        else {
            session.setUsername(username);
            sessionManager.setCookieUsername(session.getId(), username);
            String welcome = String.format("Tere tulemast, %s! Kasuta /help, et näha erinevaid käske.", session.getUsername());
            sendMessage(session, new MessageFormatter(welcome, MessageFormatter.GREEN));
        }
    }

    // Edastab sõnumi kõigile kasutajatele, kes on saatjaga samas ruumis
    public void broadcastToRoom(ClientSession sender, String message) {
        long otherUsers = sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> !s.equals(sender))
                .count();

        if (otherUsers == 0) {
            sendMessage(sender, new MessageFormatter("Siin pole kedagi!", MessageFormatter.ORANGE));
            return;
        }

        String formatted = String.format("[%s] %s: %s",
                sender.getCurrentRoom().getName(),
                sender.getUsername(),
                message);

        sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> s.getWebSocketSession() != null && s.getWebSocketSession().isOpen())
                .forEach(s -> sendMessage(s, new MessageFormatter(formatted, WET_ASPHALT)));
    }

    public void sendMessage(ClientSession session, MessageFormatter msg) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            session.getWebSocketSession().sendMessage(new TextMessage(json));
        } catch (Exception e) {
            logger.error("Sõnumi saatmisel tekkis error", e);
        }
    }

    private void sendUsernameMessage(ClientSession session, String msg) {
        try {
            session.getWebSocketSession().sendMessage(new TextMessage(msg));
        } catch (Exception e) {
            logger.error("Sõnumi saatmisel tekkis error", e);
        }
    }


}