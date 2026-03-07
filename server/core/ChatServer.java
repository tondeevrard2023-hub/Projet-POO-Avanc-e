package core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.Message;
import model.MessageText;
import model.User;

public class ChatServer {
    private static final int PORT = 12345;

    // liste sync
    private static Set<ObjectOutputStream> clients = Collections.synchronizedSet(new HashSet<>());
    private static Map<ObjectOutputStream, User> objetClients = new HashMap<>();

    // main
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Le serveur a demarré sur le port " +  PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Un nouvel utilisateur vient de se connecter: "+ clientSocket.getInetAddress());
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

    // diffusion
    public static void broadcast(Message message, ObjectOutputStream sender) {
        synchronized(clients) {
            for (ObjectOutputStream client : clients) {
                if (client != sender) {
                    try {
                        client.writeObject(message);
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                }
            }
        }
    }
    public static void annonce(String msg) {
        String timestamp = LocalTime.now().toString().substring(0, 5);
        String msgFormated = "📢 [" + timestamp + "] " + msg;

        synchronized(clients) {
            for (ObjectOutputStream client : clients) {
                try {
                    client.writeObject(new MessageText(msgFormated));
                    
                } catch (Exception e) {
                    System.err.println(e.toString());
                }
            }
        }
    }

    // add and remove user
    public static void addUser(ObjectOutputStream out) {
        clients.add(out);
    }
    public static void removeUser(ObjectOutputStream out) {
        clients.remove(out);
    }
    
    // getters
    public static Set<ObjectOutputStream> getClients() {
        return clients;
    }
    public static Map<ObjectOutputStream, User> getNomsClients() {
        return objetClients;
    }

}