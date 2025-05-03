package com.online_chat.websocket;

import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.MessageProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
// extends TextWebSocketHandlerit, et töödelda tekstipõhiseid sõnumeid Websocketi kaudu
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Hoiab kõiki WebSocketi sessioone, mis on aktiivsed.
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ClientSessionManager sessionManager;
    private final MessageProcessor messageProcessor;
    private final ChatRoomManager chatRoomManager;


    public ChatWebSocketHandler(ClientSessionManager sessionManager, MessageProcessor messageProcessor, ChatRoomManager chatRoomManager) {
        this.sessionManager = sessionManager;
        this.messageProcessor = messageProcessor;
        this.chatRoomManager = chatRoomManager;
    }

    // Meetod, mis aktiveeritakse, kui kasutaja loob brauseris ühenduse.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Sessiooni ID, mis lisati HandshakeInterceptori kaudu
        String sessionId = (String) session.getAttributes().get("sessionId");

        if (sessionId == null || sessionId.isBlank()) {
            session.close();
            return;
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
                messageProcessor.sendWelcomeMessage(clientSession);
            }

            sessionManager.registerSession(clientSession);
        }

        sessions.add(session);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
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

        String payload = message.getPayload();
        // Websocket ping pong, et ühendus ei kaoks
        if ("ping".equalsIgnoreCase(payload)) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage("pong"));
            }
            return;
        }


        messageProcessor.processAndBroadcast(clientSession, payload);
    }
}