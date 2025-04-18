package Spring.Commands;

import Spring.Chatroom.ChatRoomManager;
import Spring.Server.ClientSession;

public class LeaveCommand implements Command {

    private final ChatRoomManager chatRoomManager;

    public LeaveCommand(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        if (!inARoom(session)) return "Sa ei ole üheski ruumis.";

        chatRoomManager.removeClientFromCurrentRoom(session);
        return "Lahkusid ruumist.";
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 1;
    }
}