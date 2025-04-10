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
        if (username == null || username.isEmpty()) {
            return "Kasutajanimi ei saa olla tühi, vali uus.";
        }
        for (ClientSession user : sessions.values()) {
            if (username.isEmpty() || username == null) {
                return "Kasutaja nimi ei saa olla tühi, proovi uuesti.";
            }
            if (username.equalsIgnoreCase(user.getUsername())) { //Kontrollib, et kasutajanimi ei kattuks juba mõne aktiivse kasutajanimega.
                return "See kasutajanimi on juba võetud. Proovi uuesti: ";
            } else if (username.contains("/")) {
                return "Vigane kasutajanimi. Proovi uuesti: ";
            }

        }
        session.setUsername(username);
        return null;
    }
}
