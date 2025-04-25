package com.online_chat.controller;


import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;
import com.online_chat.service.MessageProcessor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
    @PostMapping("/flask")
    public ResponseEntity<String> proxyToFlaskChat(
            @RequestBody FlaskRequestPayload payload
    ) {
        String flaskUrl = "http://localhost:5001/chat";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "user_id", payload.getUserId(),
                "prompt", payload.getPrompt()
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Viga Flaski ühendamisel: " + e.getMessage());
        }
    }
}
