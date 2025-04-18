package com.online_chat.websocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.MessageProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
// extends TextWebSocketHandlerit, et töödelda tekstipõhiseid sõnumeid Websocketi kaudu
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Hoiab kõiki WebSocketi sessioone, mis on aktiivsed.
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ClientSessionManager sessionManager;
    private final MessageProcessor messageProcessor;


    public ChatWebSocketHandler(ClientSessionManager sessionManager, MessageProcessor messageProcessor) {
        this.sessionManager = sessionManager;
        this.messageProcessor = messageProcessor;
    }

    // Meetod, mis aktiveeritakse, kui kasutaja loob brauseris ühenduse.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String sessionId = getSessionIdFromQuery(session);

        // Loob uue Clientsessioni
        ClientSession clientSession = new ClientSession(sessionId);
        sessionManager.registerSession(clientSession);

        // Seob WebsocketSessioni ClientiSessioniga
        clientSession.setWebSocketSession(session);

        // lisab WebSocketSessioni aktiivsete seasioonide hulka
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
                    sessionManager.removeSession(cs.getId());
                });
    }

    // tagastab HTML-i poolt kaasa pakitud ühenduse ID
    private String getSessionIdFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("sessionId=")) {
            return query.substring("sessionId=".length());
        }
        return null;
    }
    // Käivitatakse iga kord, kui klient saadab serverile WebSocketi kaudu sõnumi
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = getSessionIdFromQuery(session);
        if (sessionId == null || sessionId.isBlank()) return; // kui sessioni puudub, siis ei töödelda sõnumit

        // otsitakse vastav Clientsession ning kui WebSocketSession pole Clientsessioniga seotud jääb protsess pooleli.
        ClientSession clientSession = sessionManager.getSession(sessionId);
        if (clientSession == null) return;


        // Sõnumi sisu HTML-st
        String payload = message.getPayload();
        // sõnum edastatakse MessageProcessorile, kes tegeleb käsu realiseerumisega
        messageProcessor.processAndBroadcast(clientSession, payload);
    }
}