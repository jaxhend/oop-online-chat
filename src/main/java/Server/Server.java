package Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class Server {

    static final int PORT = 45367;

    public static void main(String[] args) throws Exception {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        final SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();


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
                            p.addLast(sslCtx.newHandler(ch.alloc())); // SSL lisatakse enne ServerHandleri.
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