package com.online_chat.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class HttpsHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Kontrollime, kas tegemist on Servleti-põhise päringuga
        if (request instanceof ServletServerHttpRequest servletRequest &&
                response instanceof ServletServerHttpResponse servletResponse) {

            HttpServletRequest req = servletRequest.getServletRequest();
            HttpServletResponse res = servletResponse.getServletResponse();

            String sessionId = null;

            // kontrollime, kas cookie on juba olemas
            if (req.getCookies() != null) {
                for (Cookie cookie : req.getCookies()) {
                    if ("sessionId".equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                        break;
                    }
                }
            }

            // kui cookiet ei ole siis genereerime uue
            if (sessionId == null || sessionId.isBlank()) {
                sessionId = UUID.randomUUID().toString();

                Cookie cookie = new Cookie("sessionId", sessionId);
                cookie.setHttpOnly(true); // ainult server pääseb ligi
                cookie.setSecure(true); // edastatakse ainult HTTPS kaudu
                cookie.setPath("/"); // kehtib meie domeenis
                cookie.setMaxAge(7 * 24 * 60 * 60); // kehtib 7 päeva

                // lisame cookie HTTP vastusesse
                res.addCookie(cookie);
            }

            attributes.put("sessionId", sessionId);
        }

        // Lubame WebSocketi ühenduse loomise
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // meil pole vaja praegu siia midagi lisada
    }
}