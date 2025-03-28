package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.Scanner;

public class Client2 {
    private static final int TYPE_ECHO = 1;
    private static final int OK = 200;
    private static final int ERROR = 400;

    public static void main(String[] args) throws IOException {
        System.out.println("Connecting to server");
        // server: 35.212.230.59 ehk oop.atlante.ee
        try (Socket socket = new Socket("localhost", 1337);
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Connected");
            System.out.print("Sisestage kasutajanimi: ");
            String userName = scanner.nextLine();
            dout.writeUTF(userName);
            System.out.println("Edukalt sisselogitud!");
            Thread listenerThread = new Thread(() -> {
                try {
                    while (true) {
                        int responseCode = din.readInt();
                        if (responseCode == OK) {
                            String message = din.readUTF();
                            System.out.println(message);
                        } else {
                            System.out.println("Server saatis tundmatu vastuse.");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Serveriga ühendus katkes.");
                }
            });
            listenerThread.start();


            while (true) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                dout.writeInt(TYPE_ECHO);
                dout.writeUTF(message);
            }
            System.out.println("Finished");
            socket.close();
        }
    }

    public static void echo(String[] args, int i, DataInputStream din, DataOutputStream dout) throws IOException {
        dout.writeInt(Client2.TYPE_ECHO);
        dout.writeUTF(args[i + 1]);
        if (din.readInt() == OK)
            System.out.println(din.readUTF());
    }

}