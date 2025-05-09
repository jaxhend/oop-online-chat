package com.online_chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_chat.bots.weatherBot.WeatherAPI;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.model.UsernameRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import static com.online_chat.service.ColoredMessage.WET_ASPHALT;

@Service
public class MessageProcessor {

    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(WeatherAPI.class);
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
        if (session.getUsername() == null || session.getUsername().isBlank()) {
            handleUsernameAssignment(session, message);

        } else if (message.startsWith("/")) {
            ColoredMessage response = commandHandler.handle(session, message);
            sendMessage(session, response);

        } else if (session.getCurrentRoom() != null) {
            broadcastToRoom(session, message);

        } else {
            String error = """
                    Sa ei ole üheski vestlusruumis. Kasuta käske:
                    /join <chatruumi_nimi>, et liituda või luua vestlusruumiga,
                    /private <kasutaja_nimi>, et alustada privaatsõnumit.""";
            sendMessage(session, new ColoredMessage(error, ColoredMessage.ERRORS));
        }
    }

    // kasutajanime töötlemine ja sobivuse kontroll
    public void handleUsernameAssignment(ClientSession session, String message) {
        String username = message.trim();

        if (username.isBlank() || username.contains("/") || username.contains(" ") || username.contains("-"))
            sendMessage(session, new ColoredMessage("Kasutajanimi on keelatud või vales formaadis.", ColoredMessage.ERRORS));
        else if (!usernameRegistry.register(username, session.getId()))
            sendMessage(session, new ColoredMessage("Kasutajanimi on keelatud või vales formaadis.", ColoredMessage.ERRORS));
        else {
            session.setUsername(username);
            sessionManager.setCookieUsername(session.getId(), username);
            String welcome = String.format("Tere tulemast, %s! Kasuta /help, et näha erinevaid käske.", session.getUsername());
            sendMessage(session, new ColoredMessage(welcome, ColoredMessage.GREEN));
        }
    }

    // Edastab sõnumi kõigile kasutajatele, kes on saatjaga samas ruumis
    public void broadcastToRoom(ClientSession sender, String message) {
        long otherUsers = sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> !s.equals(sender))
                .count();

        if (otherUsers == 0) {
            sendMessage(sender, new ColoredMessage("Siin pole kedagi!", ColoredMessage.ORANGE));
            return;
        }

        String formatted = String.format("[%s] %s: %s",
                sender.getCurrentRoom().getName(),
                sender.getUsername(),
                message);

        sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> s.getWebSocketSession() != null && s.getWebSocketSession().isOpen())
                .forEach(s -> sendMessage(s, new ColoredMessage(formatted, WET_ASPHALT)));
    }

    public void sendMessage(ClientSession session, ColoredMessage msg) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            session.getWebSocketSession().sendMessage(new TextMessage(json));
        } catch (Exception e) {
            logger.error("Sõnumi saatmisel tekkis error", e);
        }
    }

}