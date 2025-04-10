package Messages.Commands;

import Chatroom.ChatRoomManager;
import Server.ClientSession;

public class LeaveCommand implements CommandHandler {
    private final static String LEAVE_COMMAND = "/leave";
    private ChatRoomManager roomManager;

    public LeaveCommand(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith(LEAVE_COMMAND);
    }

    @Override
    public String handle(ClientSession session, String input) {
        roomManager.removeClientFromCurrentRoom(session); // Saadab teistele ruumis olijatele sõnumi.
        return "Lahkusid ruumist."; // Kasutaja saab sõnumi, et on ruumist lahkunud.
    }
}
