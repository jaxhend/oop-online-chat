package Client;

import Chatroom.ChatRoom;
import io.netty.channel.ChannelHandlerContext;

public class ClientSession {

    private final ChannelHandlerContext ctx;
    private String username = null;
    private ChatRoom currentRoom = null;

    public ClientSession(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ChatRoom getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(ChatRoom currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void sendMessage(String message) {
        ctx.writeAndFlush(message);
    }
}
