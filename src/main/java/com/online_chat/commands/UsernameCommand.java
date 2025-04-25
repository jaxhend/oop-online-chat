package com.online_chat.commands;


import com.online_chat.model.ClientSession;
import com.online_chat.model.ClientSessionManager;

public class UsernameCommand implements Command {

    private final ClientSessionManager sessionManager;

    public UsernameCommand(ClientSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public String execute(ClientSession session, String[] args) {
        if (!validCommand(args)) return "Kasutus: /username <kasutajanimi>";

        String newUsername = args[1];
        if (sessionManager.inValidUserName(newUsername)) {
            return "Kasutajanimi on keelatud või juba kasutusel.";
        }

        session.setUsername(newUsername);
        return "Sinu kasutajanimi on nüüd: " + newUsername;
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 2;
    }
}