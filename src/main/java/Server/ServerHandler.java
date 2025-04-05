package Server;

import Chatroom.ChatRoomManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    // Thread-safe, kasutajatenimekiri
    private static final ConcurrentHashMap<Channel, ClientSession> sessions = new ConcurrentHashMap<>();
    private final MessageProcessor processor;

    public ServerHandler() {
        this.processor = new MessageProcessor(new ChatRoomManager());
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception { // Käivitub kliendi ühendamisel.
        sessions.put(ctx.channel(), new ClientSession(ctx)); // Kanal lisatakse sõnastikku.
        System.out.println("Uus klient ühendatud: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    // Serverisse sissetuleva sõnumi edastamine
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ClientSession clientSession = sessions.get(ctx.channel());
        String input = (String) msg;

        String response = processor.processMessage(clientSession, input);
        if (response != null) {
            ctx.writeAndFlush(response); // Saadab kasutajale personaalsed teated.
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}