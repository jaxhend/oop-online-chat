package Spring.Commands;

import Spring.Server.ClientSession;

public interface Command {
    String execute(ClientSession session, String[] args);

    boolean validCommand(String[] args);

    default boolean inARoom(ClientSession session) {
        return session.getCurrentRoom() != null;
    }

    default boolean hasMessageContent(String[] args) {
        return args.length >= 2;
    }

    default String extractMessage(String[] args) {
        return String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
    }


    default String getJoinOrPrivateMessage() {
        return "Sa ei ole üheski vestlusruumis.\n" +
                "Kasuta käsku /join <chatruumi_nimi>, et liituda grupiga.\n" +
                "Või kasuta /private <kasutaja_nimi>, et alustada privaatsõnumit.";
    }
}