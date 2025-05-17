package com.online_chat.commands;


import com.online_chat.chatrooms.ChatRoom;
import com.online_chat.chatrooms.ChatRoomManager;
import com.online_chat.client.ClientSession;
import com.online_chat.chatrooms.PrivateChatRoom;
import com.online_chat.model.MessageFormatter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.online_chat.chatrooms.ChatRoomManager.defaultRooms;

public class SeeChatroomsCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public SeeChatroomsCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        Map<String, ChatRoom> roomInfo = chatRoomManager.getRoomInfo();

        String result = Stream.of(
                        defaultRooms.stream()
                                .map(name -> name + " (" + chatRoomManager.getRoom(name).getClientsCount() + ")"),

                        roomInfo.entrySet().stream()
                                .filter(entry -> !defaultRooms.contains(entry.getKey()) && entry.getValue().isPublicChatRoom()) // välista default ruumid
                                .map(entry -> entry.getKey() + " (" + entry.getValue().getClientsCount() + ")"),

                        roomInfo.entrySet().stream()
                                .filter(entry -> entry.getValue().canJoin(session) && entry.getValue() instanceof PrivateChatRoom)
                                .map(entry -> entry.getKey() + " (" + entry.getValue().getClientsCount() + ")")
                )
                .flatMap(s -> s)  // Teeme üheks streamiks
                .collect(Collectors.joining(", "));

        return new MessageFormatter("Saadaval vestlusruumid: " + result, MessageFormatter.PURPLE);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}