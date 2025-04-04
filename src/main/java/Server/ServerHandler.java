package Server;

import Chatroom.ChatRoomManager;
import Client.ClientSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    // Thread-safe
    private static final ConcurrentHashMap<Channel, ClientSession> sessions = new ConcurrentHashMap<>();
    private static ChatRoomManager roomManager = new ChatRoomManager();

    public ServerHandler(ChatRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception { // Käivitub kliendi ühendamisel.
        sessions.put(ctx.channel(), new ClientSession(ctx)); // Kanal lisatakse sõnastikku.
        System.out.println("Uus klient ühendatud: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ClientSession clientSession = sessions.get(ctx.channel());
        String input = (String) msg;
        if (clientSession.getUsername() == null) {
            clientSession.setUsername((String) msg);
        } else
            ctx.writeAndFlush(clientSession.getUsername().trim() + ": " + input);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}