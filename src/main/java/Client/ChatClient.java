package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import org.snf4j.core.SelectorLoop;
import org.snf4j.core.session.IStreamSession;

// Allikas: https://github.com/snf4j/snf4j
public class ChatClient {
    static final String HOST = "localhost";
    static final int PORT = 45367;
    static final Integer BYE_TYPED = 0; // Chatist väljumiseks

    public static void main(String[] args) throws Exception {
        // Ootab evente socketidel ja seejärel väljastab neid. Tal on enda thread.
        // Kasutatakse tavaliselt koos SocketChanneliga.
        SelectorLoop loop = new SelectorLoop();

        try {
            loop.start();

            // Avab uue socketchanneli.
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false); // Mitteblokeeruv
            channel.connect(new InetSocketAddress(InetAddress.getByName(HOST), PORT));

            // Socketchanneli lisamine.
            IStreamSession session = (IStreamSession) loop
                    .register(channel, new ChatClientHandler())
                    .sync() // Ootab, kuni lisamine on lõpule viidud.
                    .getSession(); // Tagastab kanali sessiooni.

            // Ootab, kuni ühendus on valmis.
            session.getReadyFuture().sync();
            System.out.println("Ühendus loodud");

            // Loeb konsoolist.
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = in.readLine()) != null) {
                if (session.isOpen()) {
                    session.write((line).getBytes(StandardCharsets.UTF_8)); // Saadab sõnumi.
                }
                if ("bye".equalsIgnoreCase(line)) {
                    session.getAttributes().put(BYE_TYPED, BYE_TYPED);
                    break;
                }
            }
        }
        finally {
            loop.stop();
        }
    }
}
