package manager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.Message;
import model.MessageText;
import model.User;

public class ClientManager {
    // liste sync
    private static Set<ObjectOutputStream> clients = Collections.synchronizedSet(new HashSet<>());
    private static Map<ObjectOutputStream, User> objetClients = new HashMap<>();

    // add and remove user
    public static void addUser(ObjectOutputStream out) {
        clients.add(out);
    }
    public static void removeUser(ObjectOutputStream out) {
        clients.remove(out);
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


    // getters
    public static Set<ObjectOutputStream> getClients() {
        return clients;
    }
    public static Map<ObjectOutputStream, User> getNomsClients() {
        return objetClients;
    }
}
