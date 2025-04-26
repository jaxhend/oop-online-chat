package com.online_chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/flask")
public class ChatRestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/päevapakumised")
    public ResponseEntity<String> getDeals() {
        String flaskResponse = restTemplate.getForObject("http://localhost:5000/flask/päevapakumised", String.class);
        return ResponseEntity.ok(flaskResponse);
    }

    @GetMapping("/ilm")
    public ResponseEntity<String> getWeather() {
        String flaskResponse = restTemplate.getForObject("http://localhost:5000/flask/ilm", String.class);
        return ResponseEntity.ok(flaskResponse);
    }

    @GetMapping("/uudised")
    public ResponseEntity<String> getNews() {
        String flaskResponse = restTemplate.getForObject("http://localhost:5000/flask/uudised", String.class);
        return ResponseEntity.ok(flaskResponse);
    }

    @PostMapping("/chatbot")
    public ResponseEntity<String> talkToBot(@RequestBody String userMessage) {
        String botResponse = restTemplate.postForObject(
                "http://localhost:5000/flask/chat",
                userMessage,
                String.class
        );
        return ResponseEntity.ok(botResponse);
    }

    @GetMapping("/õppekava")
    public ResponseEntity<String> getCurriculum() {
        String flaskResponse = restTemplate.getForObject("http://localhost:5000/flask/õppekava", String.class);
        return ResponseEntity.ok(flaskResponse);
    }
}