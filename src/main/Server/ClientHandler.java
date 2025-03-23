package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {
    private static final int TYPE_ECHO = 1;
    private static final int TYPE_FILE = 2;
    private static final int OK = 200;
    private static final int ERROR = 400;
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        long threadId = Thread.currentThread().threadId();
        System.out.println("Thread " + threadId + " started");

        try (Socket s = socket;
             DataInputStream din = new DataInputStream(s.getInputStream());
             DataOutputStream dout = new DataOutputStream(s.getOutputStream())) {

            int messageCount = din.readInt();
            for (int i = 0; i < messageCount; i++) {
                int request = din.readInt();
                if (request == TYPE_ECHO) {
                    String message = din.readUTF();
                    dout.writeInt(OK);
                    dout.writeUTF(message);
                    System.out.println("ECHO: " + message);

                } else if (request == TYPE_FILE) {
                    String filename = din.readUTF();
                    Path path = Paths.get(filename);
                    if (!path.isAbsolute() && Files.isRegularFile(path)) {
                        dout.writeInt(OK);
                        byte[] fileContent = Files.readAllBytes(path);
                        dout.writeInt(fileContent.length);
                        dout.write(fileContent);
                        System.out.println("FILE: " + filename);
                    } else {
                        dout.writeInt(ERROR);
                    }
                } else {
                    dout.writeInt(ERROR);
                }
            }
            System.out.println("Thread " + threadId + " ended");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}