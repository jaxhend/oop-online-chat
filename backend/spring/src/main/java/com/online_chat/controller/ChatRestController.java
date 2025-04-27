package com.online_chat.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
public class ChatRestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/päevapakumised")
    public ResponseEntity<String> getDeals() {
        return ResponseEntity.ok("Kohvi 1 euro");
    }

    @GetMapping("/ilm")
    public ResponseEntity<String> getWeather() {
        return ResponseEntity.ok("Tartu, 17 kraadi");

    }

    @GetMapping("/uudised")
    public ResponseEntity<String> getNews() {
        return ResponseEntity.ok(  "Online Chat töötab hästi!");
    }

    @PostMapping("/chatbot")
    public ResponseEntity<String> chatbot(@RequestBody String userMessage) {
        String botResponse = restTemplate.postForObject(
                "http://localhost:5000/flask/chat",
                userMessage,
                String.class
        );
        return ResponseEntity.ok(botResponse);
    }

}
