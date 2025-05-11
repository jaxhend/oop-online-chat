package com.online_chat.commands;


import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.service.MessageFormatter;

public class JoinRoomCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public JoinRoomCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        if (validCommand(args))
            return new MessageFormatter("Kasutus: /join <ruumi_nimi>", MessageFormatter.ERRORS);

        String roomName = args[1].toLowerCase();

        if (roomName.contains("-")) {
            return new MessageFormatter("Privaatvestluse alustamiseks või sellega liitumiseks kasuta käsku /private <kasutajanimi>", MessageFormatter.ERRORS);
        }
        if (session.getCurrentRoom() != null && roomName.equals(session.getCurrentRoom().getName())) {
            return new MessageFormatter("Oled juba ruumis '" + roomName + "'.", MessageFormatter.ERRORS);
        }

        // vajadusel loome uue ruumi ning eemaldame kliendi vanast ruumist ning lisame uude ruumi
        chatRoomManager.getOrCreatePublicRoom(roomName);
        chatRoomManager.removeClientFromCurrentRoom(session);
        chatRoomManager.addClientToRoom(session, roomName); // Lisab ruumi ja saadab sõnumi.

        return new MessageFormatter("Liitusid ruumiga '" + roomName + "'", MessageFormatter.GREEN);
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length != 2;
    }
}