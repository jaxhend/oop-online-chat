package Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public final class EchoClient {

    static final String HOST = System.getProperty("host", "oop.atlante.ee");
    static final int PORT = Integer.parseInt(System.getProperty("port", "45367"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {
        final SslContext sslCtx;
        if (System.getProperty("ssl") != null) {
            sslCtx = SslContextBuilder.forClient().build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                            }
                            p.addLast(new EchoClientHandler());
                        }
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            Channel channel = f.channel();
            System.out.println("Sisesta sõnumid, mida saata serverile:");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("quit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                channel.writeAndFlush(Unpooled.copiedBuffer(line, CharsetUtil.UTF_8));
            }

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}