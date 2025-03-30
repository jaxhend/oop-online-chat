package Server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.snf4j.core.SelectorLoop;
import org.snf4j.core.factory.AbstractSessionFactory;
import org.snf4j.core.handler.IStreamHandler;

public class ChatServer {
    static final int PORT = 45367;

    public static void main(String[] args) throws Exception {
        // Kuulab evente socketil. Saab aru, kui uus kasutaja tahab ühendada.
        SelectorLoop loop = new SelectorLoop();
        System.out.println("TESTTEST");

        try {
            loop.start();

            // Kuulaja initsialiseerimine
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(PORT));

            // Kuulaja lisamine
            // AbstractSessionFactory loob iga uue kliendi jaoks ChatServerHandleri.
            loop.register(channel, new AbstractSessionFactory() {

                @Override
                protected IStreamHandler createHandler(SocketChannel channel) {
                    return new ChatServerHandler();
                }
            }).sync();

            // Oota, kuni loop lõppeb.
            loop.join();
        }
        finally {

            loop.stop();
        }
    }
}
