import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.media.AudioClip;
import java.util.Optional;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import network.ChatClientConnection;
import model.User;
import model.Message;
import model.MessageText;
import model.MessageFile;
import ui.ChatView1;

public class ChatClientFX extends Application {

    private ChatClientConnection connection;
    private ChatView1 chatView;
    private String username;
    private Stage primaryStage;
    
    // ========== NOUVEAU : Gestion des notifications sonores ==========
    private AudioClip notificationSound;
    private AudioClip connectSound;
    private boolean soundEnabled = true;
    
    // ========== NOUVEAU : Gestion du thème ==========
    private String currentTheme = "clair"; // clair ou sombre

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        // ========== NOUVEAU : Charger les sons ==========
        loadSounds();
        
        // 1. Demander le nom d'utilisateur
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion au chat");
        dialog.setHeaderText("Bienvenue sur l'application de chat");
        dialog.setContentText("Entrez votre nom d'utilisateur:");
        
        Optional<String> result = dialog.showAndWait();
        username = result.orElse("Anonymous");
        
        // 2. Créer la vue
        chatView = new ChatView1();
        
        // ========== NOUVEAU : Appliquer le thème ==========
        applyTheme();
        
        // 3. Initialiser la connexion réseau
        initializeNetwork();
        
        // 4. Afficher la fenêtre
        updateTitle("Connexion en cours...");
        primaryStage.setScene(new Scene(chatView, 900, 600));
        primaryStage.show();
        
        // 5. Ajouter un message système
        chatView.addMessage(new MessageText("Connexion au serveur en cours...", new User("Système")));
    }
    
    // ========== NOUVEAU : Charger les sons ==========
    private void loadSounds() {
        try {
            URL notificationUrl = getClass().getResource("/sounds/notification.wav");
            URL connectUrl = getClass().getResource("/sounds/connect.wav");
            
            if (notificationUrl != null) {
                notificationSound = new AudioClip(notificationUrl.toExternalForm());
            }
            if (connectUrl != null) {
                connectSound = new AudioClip(connectUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("Sons non trouvés, mode silencieux");
            soundEnabled = false;
        }
    }
    
    // ========== NOUVEAU : Jouer un son ==========
    private void playSound(AudioClip sound) {
        if (soundEnabled && sound != null) {
            sound.play();
        }
    }
    
    // ========== NOUVEAU : Mettre à jour le titre ==========
    private void updateTitle(String status) {
        Platform.runLater(() -> {
            primaryStage.setTitle("Chat Application - " + username + " (" + status + ")");
        });
    }
    
    // ========== NOUVEAU : Appliquer le thème ==========
    private void applyTheme() {
        // Récupérer le thème depuis les préférences (à implémenter)
        // Pour l'instant, on utilise le thème clair par défaut
        
        if (currentTheme.equals("sombre")) {
            // Thème sombre
            chatView.setStyle("-fx-background-color: #1e1e1e;");
            // Autres modifications de thème...
        }
    }
    
    private void initializeNetwork() {
        connection = new ChatClientConnection();
        
        // Configurer le gestionnaire de messages reçus
        connection.setOnMessageReceived(this::handleIncomingMessage);
        
        // ========== NOUVEAU : Gestionnaire d'erreurs ==========
        Thread connectionThread = new Thread(() -> {
            try {
                connection.connect("localhost", 12345, new User(username));
                
                Platform.runLater(() -> {
                    playSound(connectSound);
                    chatView.addMessage(new MessageText("✅ Connecté au serveur !", new User("Système")));
                    chatView.setConnectionStatus(true, 1); // Mettre à jour la barre de statut
                    updateTitle("Connecté");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur de connexion", 
                             "Impossible de se connecter au serveur.\nVérifiez que le serveur est démarré.");
                    chatView.addMessage(new MessageText("❌ Échec de connexion au serveur", new User("Système")));
                    chatView.setConnectionStatus(false, 0);
                    updateTitle("Déconnecté");
                });
            }
        });
        
        connectionThread.setDaemon(true);
        connectionThread.start();
        
        // Configurer l'envoi des messages texte
        chatView.setOnSendAction(event -> sendMessage());
        
        // Configurer l'envoi des fichiers
        chatView.setOnFileSelected(event -> sendFile());
        
        // Configurer le bouton quitter
        chatView.setOnQuitAction(event -> quitChat());
    }
    
    private void handleIncomingMessage(Message message) {
        Platform.runLater(() -> {
            boolean isFromMe = false;
            
            if (message instanceof MessageText) {
                MessageText textMsg = (MessageText) message;
                isFromMe = textMsg.getSender().getName().equals(username);
                textMsg.setMine(isFromMe);
                
                if (textMsg.getContent().startsWith("@")) {
                    textMsg.setVisibility("private");
                } else {
                    textMsg.setVisibility("public");
                }
                
                chatView.addMessage(textMsg);
                
            } else if (message instanceof MessageFile) {
                MessageFile fileMsg = (MessageFile) message;
                isFromMe = fileMsg.getSender().getName().equals(username);
                fileMsg.setMine(isFromMe);
                chatView.addMessage(fileMsg);
            }
            
            // ========== NOUVEAU : Jouer un son si le message n'est pas de moi ==========
            if (!isFromMe) {
                playSound(notificationSound);
            }
        });
    }
    
    private void sendMessage() {
        MessageText message = chatView.getMessageText();
        if (message != null) {
            message.setMine(true);
            connection.send(message);
            chatView.addMessage(message);
            chatView.clearInput();
        }
    }
    
    private void sendFile() {
        if (chatView.getSelectedConversation() == null) {
            showWarning("Aucune conversation", 
                       "Veuillez sélectionner une conversation avant d'envoyer un fichier.");
            return;
        }
        
        File selectedFile = chatView.chooseFile();
        if (selectedFile != null) {
            // Vérifier la taille (limite à 10MB)
            if (selectedFile.length() > 10 * 1024 * 1024) {
                showWarning("Fichier trop volumineux", 
                           "Le fichier est trop volumineux (max 10MB)");
                return;
            }
            
            // Vérifier le type de fichier (optionnel)
            String fileName = selectedFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            // Liste des extensions autorisées
            String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "pdf", "txt", "doc", "docx", "mp3", "mp4"};
            boolean allowed = false;
            for (String ext : allowedExtensions) {
                if (ext.equals(extension)) {
                    allowed = true;
                    break;
                }
            }
            
            if (!allowed) {
                showWarning("Type de fichier non autorisé", 
                           "Ce type de fichier n'est pas autorisé.");
                return;
            }
            
            MessageFile fileMessage = chatView.createFileMessage(selectedFile, new User(username));
            
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
            // Envoyer un message de déconnexion (optionnel)
            try {
                MessageText quitMsg = new MessageText(username + " a quitté le chat", new User("Système"));
                connection.send(quitMsg);
            } catch (Exception e) {
                // Ignorer
            }
            
            // Fermer la connexion
            if (connection != null) {
                connection.closeConnection();
            }
            
            // Fermer l'application
            Platform.exit();
        }
    }
    
    // ========== NOUVELLE : Méthodes utilitaires pour les alertes ==========
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (connection != null) {
            connection.closeConnection();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}