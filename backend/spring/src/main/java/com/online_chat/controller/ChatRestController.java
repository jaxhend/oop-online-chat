package com.online_chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class ChatRestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/päevapakumised")
    public ResponseEntity<String[]> getDeals() {
        String[] deals = {
                "Kohvi 1 euro",
                "Sai 50 senti",
                "Pitsa 3 eurot"
        };
        return ResponseEntity.ok(deals);
    }

    @GetMapping("/ilm")
    public ResponseEntity<String[]> getWeather() {
        String[] weather = {
                "Tartu, 17 kraadi",
                "Pärnu, 18 kraadi",
                "Tallinn, 16 kraadi"
        };
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/uudised")
    public ResponseEntity<String[]> getNews() {
        String[] news = {
                "Online Chat töötab hästi!",
                "Täna toimub serveri hooldus.",
                "Uued funktsioonid on peatselt tulemas!"
        };
        return ResponseEntity.ok(news);
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