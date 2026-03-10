package fakeServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import model.Message;
import model.MessageFile;
import model.MessageText;
import model.User;

public class EchoServer {

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server started on port 12345");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                // Un thread par client
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
    try (
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    ) {
        User user = (User) in.readObject();
        // S'assurer que le nom n'est pas null
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName("Anonyme");
        }
        System.out.println(user.getName() + " s'est connecté !");
        
        // Créer un message système avec un expéditeur valide
        User systemUser = new User("Système");
        MessageText connectMsg = new MessageText(user.getName() + " s'est connecté !", systemUser);
        connectMsg.setVisibility("public");
        out.writeObject(connectMsg);

        while (true) {
            Message message = (Message) in.readObject();
            
            // S'assurer que l'expéditeur a un nom
            if (message.getSender() == null) {
                message.setSender(user);
            } else if (message.getSender().getName() == null) {
                message.getSender().setName(user.getName());
            }
            
            if (message instanceof MessageText msgText) {
                System.out.println("Received: " + msgText.getContent());
            } else {
                MessageFile msgFile = (MessageFile) message;
                System.out.println("Received file: " + msgFile.getFileName() + 
                                 " (" + msgFile.getData().length + " bytes)");
            }

            out.writeObject(message);
            out.flush();
        }
    } catch (Exception e) {
        System.out.println("Client déconnecté");
        e.printStackTrace();
    }
}
}