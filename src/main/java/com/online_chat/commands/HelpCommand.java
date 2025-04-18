package com.online_chat.commands;


import com.online_chat.model.ClientSession;

public class HelpCommand implements Command {

    @Override
    public String execute(ClientSession session, String[] args) {
        return """
        Saadaolevad käsud:
        /username <nimi> – muudab sinu kasutajanime
        /join <ruum> – liitub või loob uue vestlusruumi
        /leave – lahkub aktiivsest vestlusruumist
        /rooms – kuvab avalikud ruumid ja aktiivsete liikmete arvu
        /members – kuvab hetkel aktiivsed kasutajad
        /private <kasutajanimi> – alustab privaatvestlust
        /msg <sõnum> – saadab sõnumi aktiivsesse ruumi
        /help – kuvab selle abiteate
        """;
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 1;
    }
}