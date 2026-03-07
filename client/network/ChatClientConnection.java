package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;
import model.Message;
import model.User;

// import javafx.application.Platform;

public class ChatClientConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<Message> onMessageReceived;
    
    public ChatClientConnection() {
    }
    
    public void connect(String host, int port, User user) {
        try {
            socket = new Socket(host, port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(user);
            new Thread(this::listen).start();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void listen() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                if (msg != null) {
                    onMessageReceived.accept(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }

    public void send(Message message) {
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public void setOnMessageReceived(Consumer<Message> consumer) {
        this.onMessageReceived = consumer;
    }
}