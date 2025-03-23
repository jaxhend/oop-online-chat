package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.Scanner;

public class Client {
    private static final int TYPE_ECHO = 1;
    private static final int OK = 200;
    private static final int ERROR = 400;

    public static void main(String[] args) throws IOException {
        System.out.println("Connecting to server");
        // server: 35.212.230.59 ehk oop.atlante.ee
        try (Socket socket = new Socket("oop.atlante.ee", 45367);
            DataInputStream din = new DataInputStream(socket.getInputStream());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected");

            while (true) {
                System.out.println("Enter message: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                dout.writeInt(TYPE_ECHO);
                dout.writeUTF(message);

                int responseCode = din.readInt();
                if(responseCode == OK){
                    System.out.println("Server response: " + din.readUTF());
                } else {
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

}