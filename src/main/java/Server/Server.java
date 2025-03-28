package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("Now listening on :45367");
            System.out.println("Current working directory: " + System.getProperty("user.dir"));

            while (true) {
                Socket s = ss.accept();
                System.out.println("Connected: " + s.getInetAddress());
                Thread client = new Thread(new ClientHandler(s));
                client.start();
            }
        }
    }
}
