package com.online_chat.commands;


import com.online_chat.chatrooms.ChatRoomManager;
import com.online_chat.chatrooms.PrivateChatRoom;
import com.online_chat.client.ClientSession;
import com.online_chat.client.ClientSessionManager;
import com.online_chat.model.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Arrays;

import static com.online_chat.service.MessageProcessor.objectMapper;

public class PrivateJoinCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(PrivateJoinCommand.class);
    private final ChatRoomManager roomManager;
    private final ClientSessionManager sessionManager;

    public PrivateJoinCommand(ChatRoomManager roomManager, ClientSessionManager sessionManager) {
        this.roomManager = roomManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        // kontrollime kasutaja sisendit, et vastaks nõuetele
        if (validCommand(args))
            return new MessageFormatter("Kasutus: /privaat <kasutajanimi>", MessageFormatter.RED);

        if (args[1].equalsIgnoreCase(session.getUsername()))
            return new MessageFormatter("Sa ei saa luua privaatvestlust iseendaga", MessageFormatter.RED);

        ClientSession target = sessionManager.getAllSessions().stream()
                .filter(s -> args[1].equalsIgnoreCase(s.getUsername()))
                .findFirst().orElse(null);

        if (target == null)
            return new MessageFormatter("Ei leitud kasutajat '" + args[1] + "'", MessageFormatter.RED);

        if (session.getCurrentRoom() != null && session.getCurrentRoom().equals(target.getCurrentRoom()) &&
                session.getCurrentRoom() instanceof PrivateChatRoom)
            return new MessageFormatter("Oled juba privaatvestluses " + args[1], MessageFormatter.RED);

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

        String text = existed
                ? "Liitusid olemasoleva privaatvestlusega kasutajaga \"" + args[1] + "\"."
                : "Privaatvestlus kasutajaga \"" + args[1] + "\" on loodud.";
        return new MessageFormatter(text, MessageFormatter.GREEN);
    }

    // Teisele osapoolele teavituse saatmine
    private void sendInvite(ClientSession target, String fromUsername) {
        if (target.getWebSocketSession() != null && target.getWebSocketSession().isOpen()) {
            try {
                String text = "Kasutaja '" + fromUsername + "' alustas sinuga privaatvestlust. " +
                        "Liitumiseks kasuta käsku: /privaat " + fromUsername;
                String json = objectMapper.writeValueAsString(new MessageFormatter(text, MessageFormatter.GREEN));
                target.getWebSocketSession()
                        .sendMessage(new TextMessage(json));
            } catch (IOException e) {
                logger.error("Privaatsõnumi kutse saatmise error", e);
            }
        }
    }

    // Genereerib privaatse ruumi ID kahe kasutajanime põhjal.
    private String buildPrivateRoomId(String user1, String user2) {
        String[] users = {user1.toLowerCase(), user2.toLowerCase()};
        Arrays.sort(users);
        return users[0] + "-" + users[1];
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length != 2;
    }
}
