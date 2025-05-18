package com.online_chat.commands;


import com.online_chat.client.ClientSession;
import com.online_chat.model.MessageFormatter;

public class HelpCommand implements Command {

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        String text = """
                Saadaval käsud:
                /join <ruum> – liitu või loo uus vestlusruum
                /private <kasutajanimi> – alusta või liitu privaatvestlusega
                /leave – lahku aktiivsest vestlusest
                /chatrooms – kuva avalikud vestlusruumid
                /members – kuva hetkel aktiivsed kasutajad
                """;
        return new MessageFormatter(text, MessageFormatter.PURPLE);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}