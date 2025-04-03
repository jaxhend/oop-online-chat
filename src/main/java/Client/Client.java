package Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Client {
    static final String HOST = "oop.atlante.ee";
    static final int PORT = 45367;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("Decoder", new StringDecoder(StandardCharsets.UTF_8));
                    p.addLast("Encoder", new StringEncoder(StandardCharsets.UTF_8));
                    p.addLast("Handler", new ClientHandler());
                }
            });

            Channel channel = b.connect(HOST, PORT).sync().channel();

            System.out.println("Sisesta username ja järgmisel real sõnum:  ");

            //TODO: CLI
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) {
                    System.out.println("Programm lõpetas töö.");
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line + "\r\n");
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}