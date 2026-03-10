package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT = 12345;

    // main
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Le serveur a demarré sur le port " +  PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Flux IO
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                // pourquoi ne pas directement ajouter à ce niveau
                // clients.add(out);
                new Thread(new core.HandlerClient(clientSocket,out, in)).start();

            }
        } catch (IOException e) {
            System.out.println("Erreur serveur: " + e.getMessage());
        }
    }
}