package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;


public class ClientHandler implements Runnable {
    private static final int TYPE_ECHO = 1;
    private static final int OK = 200;
    private static final int ERROR = 400;
    private final Socket socket;
    private static final CopyOnWriteArrayList<DataOutputStream> clientOutputs = new CopyOnWriteArrayList<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("ClientHandler started");

        try (Socket s = socket;
             DataInputStream din = new DataInputStream(s.getInputStream());
             DataOutputStream dout = new DataOutputStream(s.getOutputStream())) {

            clientOutputs.add(dout);

            String userName = din.readUTF();
            while (true) {
                try{
                int request = din.readInt();
                System.out.println("Received request type: " + request);
                if (request == TYPE_ECHO) {
                    String message = din.readUTF();
                    dout.writeInt(OK);
                    dout.writeUTF(userName + ": " + message);
                    System.out.println("ECHO: " + message);
                    for (DataOutputStream out : clientOutputs) {
                        if (out != dout) {
                            try {
                                out.writeInt(OK);
                                out.writeUTF(userName + ": " + message);
                            } catch (IOException e) {
                                clientOutputs.remove(out);
                            }
                        }
                    }
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
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
