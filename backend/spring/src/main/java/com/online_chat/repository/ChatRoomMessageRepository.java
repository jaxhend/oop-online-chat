package com.online_chat.repository;

import com.online_chat.model.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRoomMessageRepository extends JpaRepository<ChatRoomMessage, Long> {
    // ChatRoomMessage - entity, Long - ID.

    List<ChatRoomMessage> findChatRoomMessageByChatRoomNameAndTimestampAfterOrderByTimestampAsc(String chatRoomName, LocalDateTime timestamp);

    void deleteByTimestampBefore(LocalDateTime timestampBefore);

    int countByChatRoomNameAndTimestampAfter(String chatRoomName, LocalDateTime timestampAfter);
}
