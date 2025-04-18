package Spring.Commands;

import Spring.Chatroom.ChatRoomManager;
import Spring.Server.ClientSession;
import Spring.Server.ClientSessionManager;
import org.springframework.web.socket.TextMessage;

import java.util.Arrays;

public class PrivateJoinCommand implements Command {

    private final ChatRoomManager roomManager;
    private final ClientSessionManager sessionManager;

    public PrivateJoinCommand(ChatRoomManager roomManager, ClientSessionManager sessionManager) {
        this.roomManager = roomManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        // kontrollime kasutaja sisendit, et vastaks nõuetele
        if (!validCommand(args)) return "Kasutus: /private <kasutajanimi>";
        if (args[1].equalsIgnoreCase(session.getUsername())) return "Sa ei saa luua privaatvestlust iseendaga.";

        ClientSession target = sessionManager.getAllSessions().stream()
                .filter(s -> args[1].equalsIgnoreCase(s.getUsername()))
                .findFirst().orElse(null);

        if (target == null) return "Kasutajat ei leitud: " + args[1];

        // loome privaatse ruumi
        String roomId = buildPrivateRoomId(session.getUsername(), args[1]);
        boolean existed = roomManager.roomExists(roomId);
        if (!existed) {
            sendInvite(target, session.getUsername()); // saadame teisele osapoolele teavituse sellest
        }
        // vajadusel loome uue ruumi ning eemaldame kliendi vanast ruumist ning lisame uude ruumi
        roomManager.getOrCreatePrivateRoom(roomId, session, target);
        roomManager.removeClientFromCurrentRoom(session);
        roomManager.addClientToRoom(session, roomId);

        return existed
                ? "Liitusid olemasoleva privaatvestlusega kasutajaga \"" + args[1] + "\"."
                : "Privaatvestlus kasutajaga \"" + args[1] + "\" on loodud.";
    }

    // Teisele osapoolele teavituse saatmine
    private void sendInvite(ClientSession target, String fromUsername) {
        if (target.getWebSocketSession() != null && target.getWebSocketSession().isOpen()) {
            try {
                target.getWebSocketSession().sendMessage(
                        new TextMessage("Kasutaja '" + fromUsername + "' alustas sinuga privaatvestlust. " +
                                "Liitumiseks kasuta käsku: /private " + fromUsername));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Genereerib privaatse ruumi ID kahe kasutajanime põhjal.
    private String buildPrivateRoomId(String user1, String user2) {
        String[] users = { user1.toLowerCase(), user2.toLowerCase() };
        Arrays.sort(users);
        return users[0] + "-" + users[1];
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 2;
    }
}
