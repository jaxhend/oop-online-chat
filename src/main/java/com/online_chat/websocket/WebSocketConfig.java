package com.online_chat.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // lülitab sisse WebSocket toe Springis
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;

    public WebSocketConfig(ChatWebSocketHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(chatHandler, "/ws")
                // WebSocketi ühendused aadressil /ws käsitletakse ChatwebsocketHandleri kaudu
                .setAllowedOrigins("*");
        // lubab ühenduda kõikjalt
    }
}

