package com.online_chat.commands;


import com.online_chat.model.ChatRoomManager;
import com.online_chat.model.ClientSession;
import com.online_chat.service.ColoredMessage;

public class JoinRoomCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public JoinRoomCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public ColoredMessage execute(ClientSession session, String[] args) {
        if (validCommand(args))
            return new ColoredMessage("Kasutus: /join <ruumi_nimi>", ColoredMessage.ERRORS);

        String roomName = args[1].toLowerCase();

        if (roomName.contains("-")) {
            return new ColoredMessage("Privaatvestluse alustamiseks või sellega liitumiseks kasuta käsku /private <kasutajanimi>", ColoredMessage.ERRORS);
        }
        if (session.getCurrentRoom() != null && roomName.equals(session.getCurrentRoom().getName())) {
            return new ColoredMessage("Oled juba ruumis '" + roomName + "'.", ColoredMessage.ERRORS);
        }

        // vajadusel loome uue ruumi ning eemaldame kliendi vanast ruumist ning lisame uude ruumi
        chatRoomManager.getOrCreatePublicRoom(roomName);
        chatRoomManager.removeClientFromCurrentRoom(session);
        chatRoomManager.addClientToRoom(session, roomName); // Lisab ruumi ja saadab sõnumi.

        return new ColoredMessage("Liitusid ruumiga '" + roomName + "'", ColoredMessage.GREEN);
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length != 2;
    }
}