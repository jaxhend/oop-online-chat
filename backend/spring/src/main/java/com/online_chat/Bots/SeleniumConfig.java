package com.online_chat.Bots;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfig {
    @PostConstruct
    public void setUpSeleniumCachePath() {
        System.setProperty("wdm.cachePath", "/tmp/wdm-cache");
        System.setProperty("wdm.targetPath", "/tmp/wdm-cache");
    }
}
