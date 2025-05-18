package com.online_chat.scheduler;

import com.online_chat.service.ChatRoomMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanup {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanup.class);

    private final ChatRoomMessageService chatRoomMessageService;

    @Autowired
    public DatabaseCleanup(ChatRoomMessageService chatRoomMessageService) {
        this.chatRoomMessageService = chatRoomMessageService;
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void scheduledCleanup() {
        logger.info("Andmebaasist kustutatakse vanad sõnumid.");
        chatRoomMessageService.deleteOldMessages();
        logger.info("Kustutamine on tehtud.");
    }

}
