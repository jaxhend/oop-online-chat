package Chatroom;

import Server.ClientSession;

// TODO: implement private messages
public class PrivateChatRoom extends ChatRoom {
    private ClientSession session1;
    private ClientSession session2;


    public PrivateChatRoom(String name, ClientSession session1, ClientSession session2){
        super(name, false);
        this.session1 = session1;
        this.session2 = session2;
    }

    @Override
    public void join(ClientSession session) {
    }

    @Override
    public void leave(ClientSession session) {

    }

    @Override
    public void broadcast(String message, ClientSession session, boolean isChatMessage) {

    }

    @Override
    public int activeMembers() {
        return 0;
    }
}
