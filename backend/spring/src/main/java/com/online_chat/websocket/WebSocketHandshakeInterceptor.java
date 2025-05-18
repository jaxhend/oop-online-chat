package com.online_chat.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

import static com.online_chat.controller.SessionController.cookieName;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Kontrollime, kas tegemist on Servleti-põhise päringuga
        if (request instanceof ServletServerHttpRequest servletRequest) {

            HttpServletRequest req = servletRequest.getServletRequest();
            String sessionId = null;

            // Kontrollime, kas cookie on juba olemas
            if (req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }

            if (sessionId != null && !sessionId.isBlank())
                attributes.put("sessionId", sessionId);
            else {
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false; // Paneme ühenduse kinni.
            }

        }
        // Lubame WebSocketi ühenduse loomise
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Jääb hetkel tühjaks.
    }
}