package Messages.Commands;

import Server.ClientSession;

public interface CommandHandler {

    boolean matches(String input);

    String handle(ClientSession session, String command);
}
