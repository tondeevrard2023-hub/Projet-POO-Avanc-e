package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import manager.ClientManager;
import model.Message;
import model.MessageFile;
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
            ClientManager.addUser(out);
            user = (User) in.readObject();
            
            // S'assurer que le nom n'est pas null
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName("Anonyme");
            }

            // annoncer à tous qu'il s'est connecté
            int len = ClientManager.getClients().size();
            User systemUser = new User("Système");
            String msg = user.getName() + " s'est connecté (" + len + " connecté" + (len>1? "s":"") + ")";
            MessageText connectMsg = new MessageText(msg, systemUser);
            
            connectMsg.setVisibility("public");
            ClientManager.broadcast(connectMsg, null);
            
            System.out.println(msg + " :: " + clientSocket.getInetAddress());
            
            while (true) {
                Message message = (Message) in.readObject();

                if (message instanceof MessageText msgText) {
                    System.out.println("Message reçu de " + msgText.getSender().getName() + ": " + msgText.getContent());
                } else if (message instanceof MessageFile msgFile) {
                    System.out.println("Fichier reçu de " + msgFile.getSender().getName() + ": " + msgFile.getFileName() + " (" + msgFile.getData().length + " bytes)");
                }

                ClientManager.broadcast(message, out);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.toString());
        } finally {
            this.closeConnection();
        }
    }

    private void closeConnection() {
        ClientManager.getClients().remove(out);

        if (user != null) {
            ClientManager.getNomsClients().remove(out);
            ClientManager.annonce(user.getName() + " s'est connecté.");
            System.out.println("Un utilisateur vient de se déconnecter: " + clientSocket.getInetAddress());
        }
        try {
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}