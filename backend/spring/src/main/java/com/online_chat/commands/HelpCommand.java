package com.online_chat.commands;


import com.online_chat.client.ClientSession;
import com.online_chat.model.MessageFormatter;

public class HelpCommand implements Command {

    @Override
    public MessageFormatter execute(ClientSession session, String[] args) {
        String text = """
                Saadaval käsud:
                • /liitu <ruum> – liitu olemasoleva või loo uus vestlusruum
                • /privaat <kasutajanimi> – alusta või liitu privaatvestlusega
                
                Saadaval nupud (sulgudes olevat käsku on saab ka käsitsi sisestada):
                • Vestlusruumid (/ruumid) - kuva nimekiri Sulle nähtavatest vestlusruumidest
                • Kasutajad (/kasutajad) – kuva aktiivsed kasutajad veebilehel või aktiivses ruumis
                • Lahku (/lahku) – lahku aktiivsest vestlusest
                """;
        return new MessageFormatter(text, MessageFormatter.PURPLE);
    }

    @Override
    public boolean validCommand(String[] args) {
        return false;
    }
}