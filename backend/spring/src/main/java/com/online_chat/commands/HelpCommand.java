package com.online_chat.commands;


import com.online_chat.model.ClientSession;
import com.online_chat.service.ColoredMessage;

public class HelpCommand implements Command {

    @Override
    public ColoredMessage execute(ClientSession session, String[] args) {
        String text = """
                Saadaval käsud:
                /join <ruum> – liitu või loo uus vestlusruum
                /private <kasutajanimi> – alusta või liitu privaatvestlusega
                /leave – lahku aktiivsest vestlusest
                /chatrooms – kuva avalikud vestlusruumid
                /members – kuva hetkel aktiivsed kasutajad
                """;
        return new ColoredMessage(text, ColoredMessage.COMMANDS);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}