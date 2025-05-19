package com.online_chat.config;

import com.online_chat.websocket.WebSocketHandler;
import com.online_chat.websocket.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final WebSocketHandler chatHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    public WebSocketConfiguration(WebSocketHandler chatHandler, WebSocketHandshakeInterceptor webSocketHandshakeInterceptor) {
        this.chatHandler = chatHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(chatHandler, "/ws")
                .addInterceptors(webSocketHandshakeInterceptor) // WebSocketi ühendused aadressil /ws käsitletakse ChatwebsocketHandleri kaudu
                .setAllowedOrigins("https://www.utchat.ee");

        // Localhosti jaoks
//                .setAllowedOrigins("*");
    }
}