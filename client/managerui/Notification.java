// ========== Gestion des notifications sonores ==========
package managerui;

import java.net.URL;

import javafx.scene.media.AudioClip;

public class Notification {
    
    public static AudioClip notificationSound;
    public static AudioClip connectSound;
    public static boolean soundEnabled = true;

    // ========== NOUVEAU : Charger les sons ==========
    public void loadSounds() {
        try {
            URL notificationUrl = getClass().getResource("/ressources/sounds/notification-1.mp3");
            URL connectUrl = getClass().getResource("/ressources/sounds/notification-3.mp3");
            
            if (notificationUrl != null) {
                notificationSound = new AudioClip(notificationUrl.toExternalForm());
            } else {
                System.err.println("Son de notification non trouvé");
            }
            if (connectUrl != null) {
                connectSound = new AudioClip(connectUrl.toExternalForm());
            } else {
                System.err.println("Son de connexion non trouvé");
            }

            soundEnabled = (notificationSound != null || connectSound != null);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des sons - mode silencieux");
            soundEnabled = false;
        }
    }

    // ========== NOUVEAU : Jouer un son ==========
    public static void playSound(AudioClip sound) {
        if (soundEnabled && sound != null) {
            sound.play();
        }
    }
}
