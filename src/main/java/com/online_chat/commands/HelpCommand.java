package com.online_chat.commands;


import com.online_chat.model.ClientSession;

public class HelpCommand implements Command {

    @Override
    public String execute(ClientSession session, String[] args) {
        return "Saadaolevad käsud:\n\n"
                + "/username <nimi> – muudab sinu kasutajanime\n\n"
                + "/join <ruum> – liitub või loob uue vestlusruumi\n\n"
                + "/leave – lahkub aktiivsest vestlusruumist\n\n"
                + "/chatrooms – kuvab avalikud ruumid ja aktiivsete liikmete arvu\n\n"
                + "/members – kuvab hetkel aktiivsed kasutajad\n\n"
                + "/private <kasutajanimi> – alustab privaatvestlust\n\n"
                + "/msg <sõnum> – saadab sõnumi aktiivsesse ruumi\n\n";
    }

    @Override
    public boolean validCommand(String[] args) {
        return args.length == 1;
    }
}