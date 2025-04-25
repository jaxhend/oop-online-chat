package com.online_chat.commands;


import com.online_chat.model.ClientSession;

public class ChatMessageCommand implements Command {

    @Override
    public String execute(ClientSession session, String[] args) {
        // kontrollime, kas kasutaja on chatruumis, kui ei ole saadame õpetuse, kuidas chatruumiga liituda
        if (!inARoom(session)) {
            return getJoinOrPrivateMessage();
        }

        if (!hasMessageContent(args)) {
            return "Tühi sõnum.";
        }

        String message = extractMessage(args);

        return "[" + session.getCurrentRoom().getName() + "] " + session.getUsername() + ": " + message;
    }

    @Override
    public boolean validCommand(String[] args) {
        return hasMessageContent(args);
    }

    public boolean hasMessageContent(String[] args) {
        return args.length >= 2;
    }

    public String extractMessage(String[] args) {
        return String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
    }

}