package com.online_chat.config; // Or your config package

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // For your REST API including /api/session/init
                .allowedOrigins("https://www.utchat.ee") // e.g., "https://mychat.vercel.app" or your custom domain on Vercel
                .allowedMethods("GET")
                .allowCredentials(true) // IMPORTANT for cookies
                .maxAge(3600);
    }
}