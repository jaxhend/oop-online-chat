package com.online_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@EnableScheduling
public class MainApplication {
    // käivitab Spring Booti rakenduse
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}