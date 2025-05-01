package com.online_chat.commands;


import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.online_chat.model.ChatRoomManager.defaultRooms;

public class SeeChatroomsCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public SeeChatroomsCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        Set<String> roomNames = chatRoomManager.getRoomNames();

        String result = Stream.concat(
                defaultRooms.stream()
                        .filter(roomNames::contains)
                        .map(name -> name + " (" + chatRoomManager.getRoom(name).getClientsCount() + ")"),

                roomNames.stream()
                    .filter(name -> !defaultRooms.contains(name) && !name.toLowerCase().contains(":"))
                    .map(name -> name + " (" + chatRoomManager.getRoom(name).getClientsCount() + ")")
        ).collect(Collectors.joining(", "));
        return "Saadaval vestlusruumid: " + result;
    }

    @Override
    public boolean validCommand(String[] args) {
        return true;
    }
}