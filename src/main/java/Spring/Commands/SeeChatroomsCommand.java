package Spring.Commands;

import Spring.Chatroom.ChatRoomManager;
import Spring.Server.ClientSession;

import java.util.Set;
import java.util.stream.Collectors;

public class SeeChatroomsCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public SeeChatroomsCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        Set<String> roomNames = chatRoomManager.getRoomNames();

        String result = roomNames.stream()
                .filter(name -> !name.toLowerCase().contains(":"))
                .map(name -> name + " (" + chatRoomManager.getRoom(name).getClientsCount() + ")")
                .collect(Collectors.joining(", "));

        return "Saadaval vestlusruumid: " + result;
    }

    @Override
    public boolean validCommand(String[] args) {
        return true;
    }
}