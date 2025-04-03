package Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class Server {
    private static final int PORT = 45367;

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // Võtab vastu sissetulevaid ühendusi.
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // Haldab registreeritud liiklust.
        final ServerHandler serverHandler = new ServerHandler();

        try {
            ServerBootstrap b = new ServerBootstrap(); // Abiklass, mis aitab serverit seadistada
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // Iga kliendi interaktsioon läbib pipeline'i
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // Pipeline töötab automaatselt. Netty käivitab selle iga kord, kui kanal saab andmeid lugeda või kirjutada.
                            ChannelPipeline p = ch.pipeline();
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            p.addLast(serverHandler); // Esimesel korral käivitatakse Serverhandler.channelActive()
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}