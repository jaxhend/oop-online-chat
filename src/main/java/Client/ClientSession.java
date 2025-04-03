package Client;

import io.netty.channel.ChannelHandlerContext;

public class ClientSession {

    private final ChannelHandlerContext ctx;
    private String username = null;

    public ClientSession(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
