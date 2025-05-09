package com.online_chat.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final HttpsHandshakeInterceptor httpsHandshakeInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatHandler, HttpsHandshakeInterceptor httpsHandshakeInterceptor) {
        this.chatHandler = chatHandler;
        this.httpsHandshakeInterceptor = httpsHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(chatHandler, "/ws")
                .addInterceptors(httpsHandshakeInterceptor) // WebSocketi ühendused aadressil /ws käsitletakse ChatwebsocketHandleri kaudu
                .setAllowedOrigins("*");
    }
}
