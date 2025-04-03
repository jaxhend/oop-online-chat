package Server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    // Kui klient ühendub, siis lisatakse kanal siia.
    public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // Käivitub kliendi ühendamisel.
        channels.add(ctx.channel()); // Kanal lisatakse gruppi.
        System.out.println("Uus klient ühendatud: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String received = null;
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            received = buf.toString(CharsetUtil.UTF_8);
            System.out.println("Klient (" + ctx.channel().remoteAddress() + "): " + received);
            for (Channel channel : channels) { // Broadcast sõnum
                if (channel != ctx.channel()) {
                    channel.writeAndFlush(received + "\n");
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}