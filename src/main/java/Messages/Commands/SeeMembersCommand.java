package Messages.Commands;

import Chatroom.ChatRoom;
import Server.ClientSession;
import Server.ServerHandler;

import java.util.ArrayList;
import java.util.List;

public class SeeMembersCommand implements CommandHandler {
    public static final String SEE_MEMBERS_COMMAND = "/member";


    @Override
    public boolean matches(String input) {
        return input.startsWith(SEE_MEMBERS_COMMAND);
    }

    @Override
    public String handle(ClientSession session, String input) {
        ChatRoom currentChatroom = session.getCurrentRoom();
        if (currentChatroom != null) {
            List<String> members = new ArrayList<>();
            currentChatroom.getParticipants().forEach(participant -> members.add(participant.getUsername()));

            return "Aktiivsed kasutajad ruumis " + currentChatroom.getName() + ": " + String.join(", ", members);
        }

        List<String> allUsers = ServerHandler.getAllUsernames();
        return "Aktiivsed kasutajad serveris: " + String.join(", ", allUsers);
    }

}
