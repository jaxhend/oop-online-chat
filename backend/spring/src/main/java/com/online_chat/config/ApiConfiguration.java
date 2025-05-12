package com.online_chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://www.utchat.ee")
                .allowedMethods("GET")
                .allowCredentials(true)
                .maxAge(3600);
    }

        // Localhost testimiseks
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:5173/")
//                .allowedMethods("GET")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
}
