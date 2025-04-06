package Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.jline.reader.LineReader;

import java.nio.charset.StandardCharsets;

public class ClientConnector {
    private static final String HOST = "oop.atlante.ee";
    private static final int PORT = 45367;

    private final EventLoopGroup group;
    private final LineReader reader;

    public ClientConnector(LineReader reader) {
        this.reader = reader;
        this.group = new NioEventLoopGroup();
    }

    public Channel connect() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("Decoder", new StringDecoder(StandardCharsets.UTF_8));
                        p.addLast("Encoder", new StringEncoder(StandardCharsets.UTF_8));
                        p.addLast("Handler", new ClientHandler(reader));
                    }
                });

        return b.connect(HOST, PORT).sync().channel();
    }

    public void shutdown() {
        group.shutdownGracefully();
    }
}