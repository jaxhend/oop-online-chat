package com.online_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.online_chat")
public class MainApplication {
    // käivitab Spring Booti rakenduse
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}