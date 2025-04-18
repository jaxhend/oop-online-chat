package com.online_chat.controller;


import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.MessageProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat") // Kõik selle klassi meetodid saavad URL-i alguseks /api/chat
public class ChatRestController {

    private final MessageProcessor processor;
    private final ClientSessionManager sessionManager;

    public ChatRestController(MessageProcessor processor, ClientSessionManager sessionManager) {
        this.processor = processor;
        this.sessionManager = sessionManager;
    }

    @PostMapping("/command") // meetod käivitub kui tehakse HTTP POST päringu aadressile ...../api/chat/command
    public ResponseEntity<String> executeCommand(
            @RequestParam String sessionId, // otsib päringust sessionId, et tuvastada kasutaja
            @RequestBody String command // loeb aadressist käsu
    ) {
        ClientSession session = sessionManager.getSession(sessionId);
        if (session == null) {
            session = new ClientSession(sessionId);
            sessionManager.registerSession(session);
        }
        //  Kui sessiooni veel ei eksisteeri, siis registreeritakse uus ClientSession.

        String result = processor.processAndBroadcast(session, command);
        // edastame sõnumi MessageProcessorile, kes tegeleb edasi käsu täitmisega

        return ResponseEntity.ok(result);
        // Tagastab kliendile serveri vastuse
    }
}
