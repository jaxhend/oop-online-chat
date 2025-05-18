package com.online_chat.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.online_chat.client.UsernameRegistry.LOCK_DAYS;

@RestController
public class SessionController {
    public static final String cookieName = "SESSION_ID";

    @GetMapping("/session/init")
    public Map<String, String> initializeSession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            Cookie sessionCookie = new Cookie(cookieName, sessionId);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(true);
            sessionCookie.setPath("/");
            sessionCookie.setDomain("utchat.ee");
            sessionCookie.setAttribute("SameSite", "None");
            sessionCookie.setMaxAge(LOCK_DAYS * 24 * 60 * 60);
            response.addCookie(sessionCookie);

            // Localhost testimiseks
//            Cookie sessionCookie = new Cookie(cookieName, sessionId);
//            sessionCookie.setPath("/");
//            sessionCookie.setMaxAge(LOCK_DAYS * 24 * 60 * 60);
//            response.addCookie(sessionCookie);
        }

        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);
        return sessionData;
    }
}
