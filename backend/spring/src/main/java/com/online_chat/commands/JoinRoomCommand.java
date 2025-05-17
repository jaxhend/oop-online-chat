package com.online_chat.commands;


import com.online_chat.chatrooms.ChatRoomManager;
import com.online_chat.client.ClientSession;
import com.online_chat.model.MessageFormatter;

public class JoinRoomCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public JoinRoomCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        if (validCommand(args))
            return new MessageFormatter("Kasutus: /join <ruumi_nimi>", MessageFormatter.RED);

        String roomName = args[1].toLowerCase();
        if (!roomName.matches("[a-zA-ZäöüõÄÖÜÕ0-9]+"))
            return new MessageFormatter("Vestlusruumi nimi tohib sisaldada ainult eesti tähestiku tähti ja numbreid.", MessageFormatter.RED);
        else if (roomName.length() > 30)
            return new MessageFormatter("Vestlusruumi nimi peab olema vähem kui 30 tähemärki.", MessageFormatter.RED);

        if (session.getCurrentRoom() != null && roomName.equalsIgnoreCase(session.getCurrentRoom().getName())) {
            return new MessageFormatter("Oled juba ruumis '" + roomName + "'.", MessageFormatter.RED);
        }

        // vajadusel loome uue ruumi ning eemaldame kliendi vanast ruumist ning lisame uude ruumi
        chatRoomManager.getOrCreatePublicRoom(roomName);
        chatRoomManager.removeClientFromCurrentRoom(session);
        chatRoomManager.addClientToRoom(session, roomName); // Lisab ruumi ja saadab sõnumi.

        return new MessageFormatter("Liitusid ruumiga '" + roomName + "'. Viimase 24h jooksul saadetud sõnumid:", MessageFormatter.GREEN);
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length != 2;
    }
}