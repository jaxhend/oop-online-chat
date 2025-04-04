package Chatroom;

import Client.ClientSession;

public interface ChatRoom { // Liides ChatRoom, et tulevikus oleks lihtsam muuta ruumide funktsioone ja võimalusi.

    public void join(ClientSession session);

    public void leave(ClientSession session);

    public void broadcast(String message);
}
