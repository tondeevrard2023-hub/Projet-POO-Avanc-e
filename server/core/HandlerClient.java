package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Message;
import model.MessageText;
import model.User;

public class HandlerClient implements Runnable {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;

    public HandlerClient(Socket clientSocket, ObjectOutputStream out, ObjectInputStream in) {
        this.clientSocket = clientSocket;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            // Ajouter ici
            ChatServer.addUser(out);
            user = (User) in.readObject();

            // annoncer à tous qu'il s'est connecté
            int len = ChatServer.getClients().size();
            ChatServer.annonce(user.getName() + " s'est connecté (" + len + " connecté" + (len>1? "s":"") + ")");
            
            while (true) {
                Message message = (Message) in.readObject();

                if (message instanceof MessageText msgText) {
                    System.out.println("Message reçu de " + msgText.getSender().getName() + ": " + msgText.getContent());
                } else {
                    System.out.println("Fichier reçu de " + message.getSender().getName());
                }

                ChatServer.broadcast(message, out);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.toString());
        } finally {
            this.closeConnection();
        }
    }

    private void closeConnection() {
        ChatServer.getClients().remove(out);

        if (user != null) {
            ChatServer.getNomsClients().remove(out);
            ChatServer.annonce(user.getName() + " s'est connecté.");
            System.out.println("Un utilisateur vient de se déconnecter: " + clientSocket.getInetAddress());
        }
        try {
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}