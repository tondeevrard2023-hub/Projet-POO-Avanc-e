package controller;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import model.Message;
import model.MessageText;
import model.User;
import network.ChatClientConnection;
import ui.ChatView;
import ui.UserListView;

public class ChatController1 {
    private ChatClientConnection connection;
    private ChatView chatView;
    private UserListView usersView;
    private User currentUser;

    public ChatController1(ChatView view, UserListView usersView) {
        this.connection = new ChatClientConnection();
        this.chatView = view;
        this.usersView = usersView;
        this.currentUser = new User(); // À améliorer : demander le nom à l'utilisateur

        init();
    }

    private void init() {
        // entrer le nom d'utilisateur
        showLoginDialog();

        // connexion
        connection.connect("localhost", 12345, currentUser);
        
        // écoute
        connection.setOnMessageReceived(msg -> {
            Platform.runLater(() -> {
                msg.setMine(false);
                chatView.addMessage(msg);
            });
        });

        // btn envoyer
        chatView.setOnSendAction(e -> {
            MessageText message = chatView.getMessageText();
            message.setMine(true);
            message.setSender(currentUser);
            connection.send(message);
            chatView.addMessage(message);
            chatView.clearInput();
        });

        // usersView.setOnUserSelected(user -> {
        //     loadConversation(user);
        // });

    }

    private void showLoginDialog() {
        TextInputDialog dialog = new TextInputDialog("Utilisateur");
        dialog.setHeaderText("Entrez votre nom");
        dialog.showAndWait().ifPresent(username -> this.currentUser.setName(username));
    }

    private void loadConversation(User user) {
        // Load conversation for selected user
    }

    public User getUser() {
        return currentUser;
    }
}
