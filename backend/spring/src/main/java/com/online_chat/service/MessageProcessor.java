package com.online_chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_chat.bots.weatherBot.WeatherApi;
import com.online_chat.client.ClientSession;
import com.online_chat.client.ClientSessionManager;
import com.online_chat.client.UsernameRegistry;
import com.online_chat.filter.ProfanityFilter;
import com.online_chat.model.ChatRoomMessage;
import com.online_chat.model.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.online_chat.model.MessageFormatter.*;

@Service
public class MessageProcessor {

    public static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(WeatherApi.class);
    private final CommandHandler commandHandler;
    private final ClientSessionManager sessionManager;
    private final UsernameRegistry usernameRegistry;
    private final ChatRoomMessageService chatRoomMessageService;
    private final ProfanityFilter profanityFilter;


    @Autowired
    public MessageProcessor(CommandHandler commandHandler, ClientSessionManager sessionManager,
                            UsernameRegistry usernameRegistry, ChatRoomMessageService chatRoomMessageService,
                            ProfanityFilter profanityFilter) {
        this.commandHandler = commandHandler;
        this.sessionManager = sessionManager;
        this.usernameRegistry = usernameRegistry;
        this.chatRoomMessageService = chatRoomMessageService;
        this.profanityFilter = profanityFilter;
    }

    // Töötleb kasutaja saadetud sõnumi ning edastab selle
    public void processAndBroadcast(ClientSession session, String message) {
        int MAX_MESSAGE_LENGTH = 300;
        if (session.getUsername() == null || session.getUsername().isBlank()) {
            handleUsernameAssignment(session, message);
        } else if (message.length() > MAX_MESSAGE_LENGTH) {
            sendMessage(session, new MessageFormatter("Sõnum on liiga pikk. Proovi uuesti!", MessageFormatter.RED));

        } else if (message.startsWith("/")) {
            if (profanityFilter.containsProfanity(message)) {
                sendMessage(session, new MessageFormatter("Kasutasid vulgaarseid sõnu, proovi jääda viisakaks!", MessageFormatter.RED));
                return;
            }
            MessageFormatter response = commandHandler.handle(session, message);
            // Kontrollib, kas tegemist oli /join käsuga ja leiab üles varasemad sõnumid
            List<MessageFormatter> oldMessages = getOldMessages(session, response);
            sendOldMessages(oldMessages, session, response);

        } else if (session.getCurrentRoom() != null) { // Tavasõnumi väljasaatmine.
            String msg = profanityFilter.filterMessage(
                    message.replace("\n", " ")
                            .replace("\r", " ")
                            .replaceAll("\\s+", " "));
            broadcastToRoom(session, msg);

        } else {
            String error = """
                    Sa ei ole üheski vestlusruumis. Kasuta käske:
                    /liitu <vestlusruumi_nimi>, et liituda või luua vestlusruumiga,
                    /privaat <kasutaja_nimi>, et alustada privaatsõnumit.""";
            sendMessage(session, new MessageFormatter(error, MessageFormatter.RED));
        }
    }


    // kasutajanime töötlemine ja sobivuse kontroll
    public void handleUsernameAssignment(ClientSession session, String message) {
        String username = message.trim();

        if (username.isBlank())
            sendUsernameMessage(session, "Kasutajanimi ei saa tühi olla.");
        else if (!username.matches("[a-zA-ZäöüõÄÖÜÕ0-9]+"))
            sendUsernameMessage(session, "Kasutajanimi tohib sisaldada ainult eesti tähestiku tähti ja numbreid.");
        else if (username.length() <= 3)
            sendUsernameMessage(session, "Kasutajanimi peab olema vähemalt 4 tähemärki.");
        else if (username.length() > 20)
            sendUsernameMessage(session, "Kasutajanime pikkus peab olema vähem kui 20 tähemärki.");
        else if (profanityFilter.containsProfanity(username))
            sendUsernameMessage(session, "Kasutasid vulgaarseid sõnu, proovi jääda viisakaks!");
        else if (!usernameRegistry.register(username, session.getId()))
            sendUsernameMessage(session, "Kasutajanimi on juba võetud.");
        else {
            session.setUsername(username);
            sessionManager.setCookieUsername(session.getId(), username);
            String welcome = String.format("Tere tulemast, %s! Kasuta /abi, et näha erinevaid käske.", session.getUsername());
            sendMessage(session, new MessageFormatter(welcome, MessageFormatter.GREEN));
        }
    }

    // Edastab sõnumi kõigile kasutajatele, kes on saatjaga samas ruumis (k.a endale).
    public void broadcastToRoom(ClientSession sender, String message) {
        long otherUsers = sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> !s.equals(sender))
                .count();

        if (otherUsers == 0) {
            sendMessage(sender, new MessageFormatter("Siin pole kedagi!", MessageFormatter.ORANGE));
            return;
        }

        String roomName = sender.getCurrentRoom().getName();
        String formatted = String.format("[%s] %s: %s",
                roomName,
                sender.getUsername(),
                message);
        MessageFormatter msg = new MessageFormatter(formatted, BLACK);
        chatRoomMessageService.saveMessage(roomName, sender.getUsername(), msg); // Lisame sõnumi andmebaasi

        sessionManager.getAllSessions().stream()
                .filter(s -> sender.getCurrentRoom().equals(s.getCurrentRoom()))
                .filter(s -> s.getWebSocketSession() != null && s.getWebSocketSession().isOpen())
                .forEach(s -> {
                    if (s.equals(sender)) { // Enda saadetud sõnum on sinist värvi.
                        sendMessage(s, new MessageFormatter(formatted, BLUE));
                        s.updateLastSeenMessage(roomName); // Värskendab viimase sõnumi nägemise aega.
                    } else sendMessage(s, msg);
                });

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

    private List<MessageFormatter> getOldMessages(ClientSession session, MessageFormatter response) {
        List<MessageFormatter> oldMessages = new ArrayList<>();
        String text = response.getText();
        Pattern pattern = Pattern.compile("Liitusid ruumiga '([^']*)'");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // Leiab regexi abil chatruumi nime.
            String chatRoomName = matcher.group(1);
            LocalDateTime lastSeen = session.getLastSeenTimestamp(chatRoomName);
            session.updateLastSeenMessage(chatRoomName);
            List<ChatRoomMessage> lastChatRoomMessages;
            if (lastSeen == null) // Kasutaja liitub esimest korda vestlusruumiga
                lastChatRoomMessages = chatRoomMessageService.findRoomMessages(chatRoomName);
            else
                lastChatRoomMessages = chatRoomMessageService.findRoomMessages(chatRoomName, lastSeen);
            for (ChatRoomMessage msg : lastChatRoomMessages) {
                MessageFormatter message = msg.getMessageFormatter();
                if (msg.getUsername().equals(session.getUsername()))
                    message.setColor(BLUE); // Kui sõnum on kasutaja enda oma, siis kasutame sinist värvi.
                oldMessages.add(message);
            }
        }
        return oldMessages;
    }

    private void sendOldMessages(List<MessageFormatter> oldMessages, ClientSession session, MessageFormatter response) {
        if (oldMessages.isEmpty())
            sendMessage(session, response);
        else {
            response.addText("\n✉\uFE0F Viimase 24 tunni lugemata sõnumid (" + oldMessages.size() + ")");
            sendMessage(session, response);
            oldMessages.forEach(msg -> sendMessage(session, msg));
            MessageFormatter lastMessage = new MessageFormatter("✅ Varasemad sõnumid on kuvatud", GREEN);
            lastMessage.removeTime(); // Eemaldame kellaaja.
            sendMessage(session, lastMessage);
        }
    }
}
