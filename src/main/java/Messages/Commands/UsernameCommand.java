package Messages.Commands;

import Server.ClientSession;

import static Server.ServerHandler.sessions;

public class UsernameCommand implements CommandHandler {

    @Override
    public boolean matches(String input) {
        return true; // Alati käivitab meetodi kui kasutajanime pole veel valitud
    }

    @Override
    public String handle(ClientSession session, String input) {
        String username = input.trim();

        if (username.isBlank())
            return "Kasutajanimi ei saa olla tühi, proovi uuesti.";

        if (username.contains("/"))
            return "Vigane kasutajanimi. Proovi uuesti: ";

        for (ClientSession user : sessions.values()) {
            if (username.equalsIgnoreCase(user.getUsername())) {
                return "See kasutajanimi on juba võetud. Proovi uuesti: ";
            }
        }

        session.setUsername(username);
        return null;
    }

}
