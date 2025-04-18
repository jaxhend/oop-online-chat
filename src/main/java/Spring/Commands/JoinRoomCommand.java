package Spring.Commands;

import Spring.Chatroom.ChatRoomManager;
import Spring.Server.ClientSession;

public class JoinRoomCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public JoinRoomCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        if (!validCommand(args)) return "Kasutus: /join <ruumi_nimi>";

        String roomName = args[1].toLowerCase();

        if (roomName.contains("-")) {
            return "Sul ei ole õigust liituda selle privaatse ruumiga!";
        }
        if (roomName.contains(session.getUsername().toLowerCase()) && roomName.contains("-")) {
            return "See on privaatvestlus, mille osa sa oled. Liitumiseks kasuta: /private <teise_kasutaja_nimi>";
        }
        if (session.getCurrentRoom() != null && roomName.equals(session.getCurrentRoom().getName())) {
            return "Oled juba ruumis '" + roomName + "'.";
        }

        // vajadusel loome uue ruumi ning eemaldame kliendi vanast ruumist ning lisame uude ruumi
        chatRoomManager.getOrCreatePublicRoom(roomName);
        chatRoomManager.removeClientFromCurrentRoom(session);
        chatRoomManager.addClientToRoom(session, roomName); // Lisab ruumi ja saadab sõnumi.

        return "Liitusid ruumiga: " + roomName;
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 2;
    }
}