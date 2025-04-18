package Spring.Commands;

import Spring.Chatroom.ChatRoom;
import Spring.Server.ClientSession;
import Spring.Server.ClientSessionManager;

import java.util.List;
import java.util.stream.Collectors;

public class SeeMembersCommand implements Command {

    private final ClientSessionManager sessionManager;

    public SeeMembersCommand(ClientSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        ChatRoom room = session.getCurrentRoom();
        List<String> usernames;
        if (room == null) {
            usernames = sessionManager.getAllUsernames();
        } else {
            usernames = room.getClients().stream()
                    .map(ClientSession::getUsername)
                    .collect(Collectors.toList());
        }

        return "Aktiivsed kasutajad: " + usernames.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));

    }

    @Override
    public boolean validCommand(String[] args) {
        return true;
    }
}