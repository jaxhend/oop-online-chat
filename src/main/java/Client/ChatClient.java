
package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.snf4j.core.SelectorLoop;
import org.snf4j.core.session.IStreamSession;

public class ChatClient {
    static final String PREFIX = "org.snf4j.";
    static final String HOST = System.getProperty(PREFIX+"Host", "127.0.0.1");
    static final int PORT = Integer.getInteger(PREFIX+"Port", 8002);
    static final Integer BYE_TYPED = 0;

    public static void main(String[] args) throws Exception {
        SelectorLoop loop = new SelectorLoop();

        try {
            loop.start();

            // Initialize the connection
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(InetAddress.getByName(HOST), PORT));

            // Register the channel
            IStreamSession session = (IStreamSession) loop.register(channel, new ChatClientHandler()).sync().getSession();

            // Confirm that the connection was successful
            session.getReadyFuture().sync();

            // Read commands from the standard input
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = in.readLine()) != null) {
                if (session.isOpen()) {
                    session.write((line).getBytes());
                }
                if ("bye".equalsIgnoreCase(line)) {
                    session.getAttributes().put(BYE_TYPED, BYE_TYPED);
                    break;
                }
            }
        }
        finally {

            // Gently stop the loop
            loop.stop();
        }
    }
}
