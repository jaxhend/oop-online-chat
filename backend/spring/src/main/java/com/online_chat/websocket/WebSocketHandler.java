package com.online_chat.websocket;

import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.MessageFormatter;
import com.online_chat.service.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
// extends TextWebSocketHandlerit, et töödelda tekstipõhiseid sõnumeid Websocketi kaudu
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    // Hoiab kõiki WebSocketi sessioone, mis on aktiivsed.
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ClientSessionManager sessionManager;
    private final MessageProcessor messageProcessor;
    private final ChatRoomManager chatRoomManager;


    public WebSocketHandler(ClientSessionManager sessionManager, MessageProcessor messageProcessor, ChatRoomManager chatRoomManager) {
        this.sessionManager = sessionManager;
        this.messageProcessor = messageProcessor;
        this.chatRoomManager = chatRoomManager;
    }

    // Meetod, mis aktiveeritakse, kui kasutaja loob brauseris ühenduse.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Sessiooni ID, mis lisati HandshakeInterceptori kaudu
        String sessionId = (String) session.getAttributes().get("sessionId");

        try {
            if (sessionId == null || sessionId.isBlank()) {
                session.close();
            }

            // Kontrollime, kas antud sessiooni ID on juba olemas
            ClientSession clientSession = sessionManager.getSession(sessionId);
            if (clientSession != null) {
                clientSession.setWebSocketSession(session);
            } else {
                clientSession = new ClientSession(sessionId);
                clientSession.setWebSocketSession(session);

                // Kui olemas, taastame varasema kasutajanime
                String previousUsername = sessionManager.getUsername(sessionId);
                if (previousUsername != null && !previousUsername.isBlank()) {
                    clientSession.setUsername(previousUsername);
                    String welcome = String.format("Tere tulemast, %s! Kasuta /help, et näha erinevaid käske.", previousUsername);
                    messageProcessor.sendMessage(clientSession, new MessageFormatter(welcome, MessageFormatter.GREEN));
                }
                sessionManager.registerSession(clientSession);
            }
            sessions.add(session);
        } catch (IOException e) {
            logger.error("Sessiooni sulgemisel viskas errori", e);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // eemaldame WebSocketSessioni aktiivsete sessioonide hulgast
        sessions.remove(session);

        // eemaldame ka vastava ClientSessioni ClientSessionManageri sessioonide hulgast
        sessionManager.getAllSessions().stream()
                .filter(cs -> session.equals(cs.getWebSocketSession()))
                .findFirst()
                .ifPresent(cs -> {
                    // Eemaldame kliendi tema praegusest ruumist korrektselt
                    chatRoomManager.removeClientFromCurrentRoom(cs);

                    // Eemaldame ClientSessioni sessionManagerist
                    sessionManager.removeSession(cs.getId());
                });
    }

    // Käivitatakse iga kord, kui klient saadab serverile WebSocketi kaudu sõnumi
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        if (sessionId == null || sessionId.isBlank()) return;

        ClientSession clientSession = sessionManager.getSession(sessionId);
        if (clientSession == null) return;

        String payload = message.getPayload().trim();

        // Websocket ping pong, et ühendus ei kaoks
        if ("__heartbeat_ping__".equalsIgnoreCase(payload)) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("__heartbeat_pong__"));
            }
            return;
        }

        messageProcessor.processAndBroadcast(clientSession, payload);
    }
}