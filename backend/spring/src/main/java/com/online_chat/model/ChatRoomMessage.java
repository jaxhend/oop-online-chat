package com.online_chat.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatroom_messages", indexes = {@Index(name = "idx_roomName", columnList = "chatRoomName")})
public class ChatRoomMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String username;

    @Column(nullable = false)
    private String chatRoomName;

    @Embedded
    private MessageFormatter messageFormatter;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatRoomMessage() {
    }

    public ChatRoomMessage(String chatRoomName, String username, MessageFormatter msg) {
        this.chatRoomName = chatRoomName;
        this.username = username;
        this.messageFormatter = msg;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    public void setMessageFormatter(MessageFormatter messageText) {
        this.messageFormatter = messageText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
