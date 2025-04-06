package Client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jline.reader.LineReader;

public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private final LineReader reader;

    public ClientHandler(LineReader reader) {
        this.reader = reader;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        reader.printAbove(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        reader.printAbove("Viga: " + cause.getMessage());
        ctx.close();
    }
}