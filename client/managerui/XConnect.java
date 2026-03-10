package managerui;

import controller.ChatClientFX;
import javafx.application.Platform;
import ui.ChatView;

public class XConnect {

    public static void onConnect(ChatView chatView) {
        Platform.runLater(() -> {
            Notification.playSound(Notification.connectSound);
            // chatView.addMessage(new MessageText("✅ Connecté au serveur !", new User("Système")));
            chatView.setConnectionStatus(true, chatView.getUserListView().getUsers().size()); // Mettre à jour la barre de statut
            ChatClientFX.chatController1.updateTitle("Connecté");
        });  
    }

    public static void onDisconnect(ChatView chatView) {
        Platform.runLater(() -> {
            XAlert.showError("Erreur de connexion", 
                        "Impossible de se connecter au serveur.\nVérifiez que le serveur est démarré.");
            // chatView.addMessage(new MessageText("❌ Échec de connexion au serveur", new User("Système")));
            chatView.setConnectionStatus(false, chatView.getUserListView().getUsers().size()); // Mettre à jour la barre de statut
            ChatClientFX.chatController1.updateTitle("Déconnecté");
        });
    }
}