package com.online_chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class ChatRestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @CrossOrigin(origins = "*")
    @GetMapping("/päevapakumised")
    public ResponseEntity<String[]> getDeals() {
        String[] deals = {
                "Kohvi 1 euro",
                "Sai 50 senti",
                "Pitsa 3 eurot"
        };
        return ResponseEntity.ok(deals);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/ilm")
    public ResponseEntity<String[]> getWeather() {
        String[] weather = {
                "Tartu, 17 kraadi"
        };
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/uudised")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String[]> getNews() {
        String[] news = {
                "Eesti valitsus kiitis heaks uue digistrateegia aastani 2030.",
                "Tartus avati uus rohetehnoloogia innovatsioonikeskus.",
                "Tallinna börsil tõusid täna enamik aktsiaid.",
                "Ilmateenistus: nädalavahetusel saabub Eestisse soojalaine.",
                "TÜ teadlased töötavad välja uut tehisintellekti mudelit meditsiinis.",
                "Valitsus arutab täiendavaid toetusi noorte ettevõtluse edendamiseks.",
                "Eesti jalgpallikoondis alistas Läti 2:1 ja pääses finaali.",
                "Tallinnas avatakse sel nädalal rahvusvaheline filmifestival.",
                "Majandusprognoos: Eesti SKP kasvab 2025. aastal 2,5%.",
                "Haridusministeerium plaanib suurendada programmeerimise õpet koolides."
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