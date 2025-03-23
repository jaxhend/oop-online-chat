package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client {
    private static final int TYPE_ECHO = 1;
    private static final int TYPE_FILE = 2;
    private static final int OK = 200;
    private static final int ERROR = 400;

    public static void main(String[] args) throws IOException {
        int messageCount = args.length / 2;
        System.out.println("Connecting to server");

        try (Socket socket = new Socket("35.212.230.59", 45367)) {
            DataInputStream din = new DataInputStream(socket.getInputStream());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connected");

            dout.writeInt(messageCount);
            for (int i = 0; i < args.length; i += 2) {
                if (args[i].equals("echo")) {
                    echo(args, i, din, dout);
                } else if (args[i].equals("file")) {
                    file(args, i, din, dout);
                } else {
                    dout.writeInt(-1);
                    if (din.readInt() == ERROR)
                        System.out.println("Unknown command: " + args[i]);
                }
            }
            System.out.println("Finished");
        }
    }

    public static void echo(String[] args, int i, DataInputStream din, DataOutputStream dout) throws IOException {
        dout.writeInt(Client.TYPE_ECHO);
        dout.writeUTF(args[i + 1]);
        if (din.readInt() == OK)
            System.out.println(din.readUTF());
    }

    public static void file(String[] args, int i, DataInputStream din, DataOutputStream dout) throws IOException {
        dout.writeInt(TYPE_FILE);
        String filename = args[i + 1];
        dout.writeUTF(filename); // Failinimi
        int response = din.readInt();
        if (response == OK) { // OK
            int length = din.readInt();
            byte[] buf = new byte[length];
            din.readFully(buf);
            Path dir = Path.of("received");
            Files.createDirectories(dir);
            Path newFile = dir.resolve(filename);
            Files.write(newFile, buf);
            System.out.println(filename + " created");
        } else if (response == ERROR)
            System.out.println("File error: " + filename);
    }
}