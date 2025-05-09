package com.online_chat.commands;


import com.online_chat.model.ChatRoom;
import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.model.PrivateChatRoom;
import com.online_chat.service.ColoredMessage;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.online_chat.model.ChatRoomManager.defaultRooms;

public class SeeChatroomsCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public SeeChatroomsCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public ColoredMessage execute(ClientSession session, String[] args) {
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

        return new ColoredMessage("Saadaval vestlusruumid: " + result, ColoredMessage.COMMANDS);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}