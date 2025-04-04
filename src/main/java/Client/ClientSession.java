package Client;

import Chatroom.RegularChatRoom;
import io.netty.channel.ChannelHandlerContext;

public class ClientSession {

    private final ChannelHandlerContext ctx;
    private String username = null;
    private RegularChatRoom currentRoom = null;

    public ClientSession(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RegularChatRoom getCurrentRoom(){
        return currentRoom;
    }

    public void setCurrentRoom(RegularChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void sendMessage(String message) {
        ctx.writeAndFlush(message);
    }
}
