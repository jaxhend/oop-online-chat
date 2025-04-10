package Messages.Commands;

import Server.ClientSession;

public interface CommandHandler {

    boolean matches(String command);

    String handle(ClientSession session, String command);
}
