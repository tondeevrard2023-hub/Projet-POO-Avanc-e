package controller;

import java.io.File;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.Message;
import model.MessageFile;
import model.MessageText;
import model.User;
import network.ChatClientConnection;
import ui.ChatView;
import managerui.XAlert;
import managerui.XConnect;
import managerui.Notification;

public class ChatController {
    private ChatClientConnection connection;
    private ChatView chatView;
    private User user;
    private Stage primaryStage;
    
    private String ipAddress;
    private int port;

    public ChatController(ChatView view, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.connection = new ChatClientConnection();
        this.user = new User();
        this.chatView = view;
        init();
    }

    private void init() {
        showLoginDialog();
        updateTitle("Connexion en cours...");
        showConnectionDialog();

        chatView.ensurePublicGroupExists();
        
        connection.setOnMessageReceived(this::handleIncomingMessage);
        chatView.getUserListView().setOnUserSelected(this::handleUserSelected);
        chatView.setOnSendAction(event -> sendMessage());
        chatView.setOnFileSelected(event -> sendFile());
        chatView.setOnQuitAction(event -> quitChat());

        Thread connectionThread = new Thread(() -> {
            try {
                connection.connect(ipAddress, port, user);              
                XConnect.onConnect(chatView);
            } catch (Exception e) {
                XConnect.onDisconnect(chatView);
            }
        });
        
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void showLoginDialog() {
        TextInputDialog dialog = new TextInputDialog("Utilisateur");
        dialog.setHeaderText("Bienvenue sur votre application de discussion instantanée");
        dialog.setContentText("Entrez votre nom d'utilisateur:");
        
        Optional<String> result = dialog.showAndWait();
        user.setName(result.orElse("Anonymous"));
    }

    private void showConnectionDialog() {
        TextInputDialog dialogAddress = new TextInputDialog("localhost");
        TextInputDialog dialogPort = new TextInputDialog("12345");

        dialogAddress.setContentText("Entrez l'adresse ip du serveur:");
        dialogPort.setContentText("Entrez le port sur lequel est connecté votre serveur:");

        ipAddress = dialogAddress.showAndWait().orElse("localhost");        
        port = Integer.valueOf(dialogPort.showAndWait().orElse("12345")).intValue();  
    }
    
    public void updateTitle(String status) {
        Platform.runLater(() -> {
            primaryStage.setTitle("Chat Application - " + user.getName() + " (" + status + ")");
        });
    }
    
   /*  private void handleIncomingMessage(Message message) {
        Platform.runLater(() -> {
            boolean isFromMe = message.getSender().getName().equals(user.getName());
            message.setMine(isFromMe);

            String conversationId = null;
            if (message instanceof MessageText || message instanceof MessageFile) {
                if (message.getVisibility().equals("private")) {
                    conversationId = isFromMe ? 
                        chatView.getCurrentConversation().getId() : 
                        "private_" + message.getSender().getName();
                } else {
                    conversationId = "public_group";
                }
            }
            
            if (conversationId != null) {
                chatView.addMessageToConversation(conversationId, message);
            }

            if (!isFromMe) {
                Notification.playSound(Notification.notificationSound);
            }
        });
    }
 */
    private void handleIncomingMessage(Message message) {
    Platform.runLater(() -> {
        boolean isFromMe = message.getSender().getName().equals(user.getName());
        message.setMine(isFromMe);

        String conversationId = null;
        if (message instanceof MessageText || message instanceof MessageFile) {
            if (message.getVisibility() == null) {
                message.setVisibility("public");
            }
            
            if (message.getVisibility().equals("private")) {
                if (isFromMe) {
                    model.ConversationItem current = chatView.getCurrentConversation();
                    conversationId = current != null ? current.getId() : null;
                } else {
                    conversationId = "private_" + message.getSender().getName();
                }
            } else {
                conversationId = "public_group";
            }
        }
        
        if (conversationId != null) {
            // S'assurer que la conversation existe
            if (conversationId.startsWith("private_")) {
                chatView.getOrCreatePrivateConversation(conversationId, message.getSender());
            } else if (isFromMe) {
                return; // Ne pas recréer la conversation publique si elle existe déjà
            }
            chatView.addMessageToConversation(conversationId, message);
        } else {
            System.err.println("Impossible de déterminer la conversation pour le message");
        }

        if (!isFromMe) {
            Notification.playSound(Notification.notificationSound);
        }
    });
}
    
    private void sendMessage() {
        MessageText message = chatView.getMessageText();
        if (message != null) {
            message.setMine(true);
            message.setSender(user);
            connection.send(message);
            chatView.addMessage(message);
            chatView.clearInput();
        }
    }
    
    private void sendFile() {
        if (chatView.getSelectedConversation() == null) {
            XAlert.showWarning("Aucune conversation", 
                       "Veuillez sélectionner une conversation avant d'envoyer un fichier.");
            return;
        }
        
        File selectedFile = chatView.chooseFile();
        if (selectedFile != null) {
            if (selectedFile.length() > 10 * 1024 * 1024) {
                XAlert.showWarning("Fichier trop volumineux", 
                           "Le fichier est trop volumineux (max 10MB)");
                return;
            }
            
            String fileName = selectedFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "pdf", "txt", "doc", "docx", "mp3", "mp4"};
            boolean allowed = false;
            for (String ext : allowedExtensions) {
                if (ext.equals(extension)) {
                    allowed = true;
                    break;
                }
            }
            
            if (!allowed) {
                XAlert.showWarning("Type de fichier non autorisé", 
                           "Ce type de fichier n'est pas autorisé.");
                return;
            }
            
            MessageFile fileMessage = chatView.createFileMessage(selectedFile, user);
            
            if (fileMessage != null) {
                connection.send(fileMessage);
                chatView.addMessage(fileMessage);
            }
        }
    }

    private void quitChat() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Quitter le chat");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment quitter ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MessageText quitMsg = new MessageText(user.getName() + " a quitté le chat", new User("Système"));
                connection.send(quitMsg);
            } catch (Exception e) {
                // Ignorer
            }
            
            if (connection != null) {
                connection.closeConnection();
            }
            
            Platform.exit();
        }
    }

    private void handleUserSelected(User selectedUser) {
        // La vue gère déjà la création de la conversation
    }

    public User getUser() {
        return user;
    }
}