package com.online_chat.service;

import com.online_chat.model.ChatRoomMessage;
import com.online_chat.model.MessageFormatter;
import com.online_chat.repository.ChatRoomMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatRoomMessageService {
    private final ChatRoomMessageRepository messageRepository;

    @Autowired
    public ChatRoomMessageService(ChatRoomMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Salvestab chatruumi sõnumid andmebaasi.
    public ChatRoomMessage saveMessage(String chatRoomName, String username, MessageFormatter msg) {
        ChatRoomMessage message = new ChatRoomMessage(chatRoomName, username, msg);
        return messageRepository.save(message);
    }

    // Leiab chatruumi varasemad sõnumid.
    public List<ChatRoomMessage> findRoomMessages(String chatRoomName, LocalDateTime lastSeen) {
        return messageRepository.findChatRoomMessageByChatRoomNameAndTimestampAfterOrderByTimestampAsc(chatRoomName, lastSeen);
    }

    public List<ChatRoomMessage> findRoomMessages(String chatRoomName) {
        LocalDateTime twentyFourHours = LocalDateTime.now().minusHours(24);
        return messageRepository.findChatRoomMessageByChatRoomNameAndTimestampAfterOrderByTimestampAsc(chatRoomName, twentyFourHours);
    }

    @Transactional
    public void deleteOldMessages() { // Kustutab kõik sõnumid, mis on vanemad kui 24h.
        LocalDateTime twentyFourHours = LocalDateTime.now().minusHours(24);
        messageRepository.deleteByTimestampBefore(twentyFourHours);
    }

    // Leiab chatruumi sõnumite arvu.
    public int countUnreadMessages(String chatRoomName) {
        LocalDateTime twentyFourHours = LocalDateTime.now().minusHours(24);
        return messageRepository.countByChatRoomNameAndTimestampAfter(chatRoomName, twentyFourHours);
    }



}
