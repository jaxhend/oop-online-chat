package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private static final int TYPE_ECHO = 1;
    private static final int OK = 200;
    private static final int ERROR = 400;
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("ClientHandler started");

        try (Socket s = socket;
             DataInputStream din = new DataInputStream(s.getInputStream());
             DataOutputStream dout = new DataOutputStream(s.getOutputStream())) {

            while (true) {
                try{
                int request = din.readInt();
                System.out.println("Received request type: " + request);
                if (request == TYPE_ECHO) {
                    String message = din.readUTF();
                    dout.writeInt(OK);
                    dout.writeUTF(message);
                    System.out.println("ECHO: " + message);

                } else {
                    dout.writeInt(ERROR);
                }
                } catch (IOException e) {
                    System.out.println("Connection with client lost.");
                    break;
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
