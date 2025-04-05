package Client;

import io.netty.channel.Channel;
import org.jline.reader.UserInterruptException;

import static Server.ServerHandler.sessions;

public final class Client {
    public static void main(String[] args) throws Exception {
        ChatConsole console = new ChatConsole(); // JLine integratsioon
        ClientConnector networkClient = new ClientConnector(console.getReader()); // Kliendi ja serveri vaheline ühendus

        try {
            // Ühendab serveriga
            Channel channel = networkClient.connect();

            String username;
            while (true) {
                username = console.getReader().readLine("Sisesta username: ");
                if (username.trim().isEmpty() || username.contains("/"))
                    console.getReader().printAbove("Sisesta korrektne username.");
                else break;
            }
            channel.writeAndFlush(username);
            System.out.println("Abi saamiseks sisesta '/help'");

            while (true) {
                String line = console.getReader().readLine("> ");
                if (line.trim().isEmpty()) continue;

                if (line.equalsIgnoreCase("/help")) {
                    String help = """
                            Saadaval käsud:
                            /help       - Kuvab selle juhendi.
                            /room       - Liitub chatruumiga.
                            /direct     - Liitub privaatsõnumiga.
                            /leave      - Lahkub praegusest chatruumist / privaatsõnumist.
                            /quit       - Sulgeb rakenduse.
                            /members    - Loetleb kõik aktiivsed kasutajad chatruumis / privaatsõnumis.
                            /chatrooms  - Loetleb kõik serveris saadaval olevad chatruumid.""";
                    console.getReader().printAbove(help);
                    continue;
                }
                if (line.equalsIgnoreCase("/quit")) {
                    console.getReader().printAbove("Programm lõpetas töö.");
                    channel.close();
                    break;
                }
                // Saadab serverile sõnumi.
                channel.writeAndFlush(line);
            }
        } catch (UserInterruptException e) {
            System.out.println("\nKatkestatud kasutaja poolt. Programm lõpetas töö.");
        } finally {
            networkClient.shutdown();
        }
    }
}