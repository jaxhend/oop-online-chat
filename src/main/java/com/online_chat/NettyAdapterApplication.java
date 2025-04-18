package com.online_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.online_chat")
public class NettyAdapterApplication {
    // käivitab Spring Booti rakenduse ja seotud serveri
    public static void main(String[] args) {
        SpringApplication.run(NettyAdapterApplication.class, args);
    }
}