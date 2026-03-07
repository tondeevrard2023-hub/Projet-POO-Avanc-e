package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import model.Message;
import model.MessageFile;
import model.MessageText;

public class MessageCell extends ListCell<Message> {

    // Indique si la conversation est privée (pour adapter l'affichage)
    private boolean isPrivateConversation = false;
    
    // Pour la date
    private LocalDate lastMessageDate = null;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    // Couleurs
    private static final String MY_MESSAGE_BLUE = "#0078FF"; // Bleu pour mes messages
    private static final String OTHER_MESSAGE_COLOR = "#ffffff"; // Blanc
    private static final String GROUP_MESSAGE_COLOR = "#E9E9EB"; // Gris clair pour groupes
    private static final String MY_FILE_COLOR = "#e3f2fd"; // Bleu clair pour mes fichiers
    private static final String OTHER_FILE_COLOR = "#f5f5f5"; // Gris clair pour fichiers des autres

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            lastMessageDate = null;
            return;
        }
        
        // Récupérer le type de conversation depuis les propriétés de la liste
        Object typeProp = getListView().getProperties().get("conversationType");
        if (typeProp instanceof Boolean) {
            this.isPrivateConversation = (Boolean) typeProp;
        }
        
        // ========== NOUVEAU : Ajouter un séparateur de date ==========
        VBox container = new VBox();
        
        // Vérifier si on doit afficher la date
        LocalDate messageDate = message.getTimestamp().toLocalDate();
        if (lastMessageDate == null || !lastMessageDate.equals(messageDate)) {
            container.getChildren().add(createDateSeparator(messageDate));
            lastMessageDate = messageDate;
        }
        
        // Contenu du message
        if (message instanceof MessageText) {
            container.getChildren().add(createTextMessageContent((MessageText) message));
        } else if (message instanceof MessageFile) {
            container.getChildren().add(createFileMessageContent((MessageFile) message));
        } else {
            setText("Message inconnu");
            return;
        }
        
        setGraphic(container);
    }
    
    // ========== NOUVEAU : Créer un séparateur de date ==========
    private HBox createDateSeparator(LocalDate date) {
        HBox separator = new HBox();
        separator.setAlignment(Pos.CENTER);
        separator.setPadding(new Insets(10, 0, 10, 0));
        
        Label dateLabel = new Label(date.format(DATE_FORMATTER));
        dateLabel.setStyle(
            "-fx-background-color: #e9edef;" +
            "-fx-text-fill: #667781;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 4 10;"
        );
        
        separator.getChildren().add(dateLabel);
        return separator;
    }
    
    // ========== MODIFIÉ : Extraire le contenu du message texte ==========
    private HBox createTextMessageContent(MessageText message) {
        // Conteneur principal pour l'alignement
        HBox mainContainer = new HBox();
        mainContainer.setPadding(new Insets(5, 10, 5, 10));
        
        // Bulle de message
        VBox messageBubble = new VBox(3);
        messageBubble.setMaxWidth(350);
        
        // Afficher le nom de l'expéditeur seulement dans les conversations de groupe
        if (!message.isMine() && !isPrivateConversation) {
            Label senderLabel = new Label(message.getSender().getName());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            senderLabel.setStyle("-fx-text-fill: #075E54;");
            senderLabel.setPadding(new Insets(0, 0, 0, 8));
            messageBubble.getChildren().add(senderLabel);
        }
        
        // Contenu du message
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(300);
        contentLabel.setPadding(new Insets(8, 12, 8, 12));
        contentLabel.setFont(Font.font(14));
        
        // Heure formatée
        String timeStr = message.getTimestamp().format(TIME_FORMATTER);
        Label hourLabel = new Label(timeStr);
        hourLabel.setFont(Font.font(10));
        hourLabel.setStyle("-fx-text-fill: #667781;");
        hourLabel.setPadding(new Insets(0, 4, 2, 0));
        
        // Conteneur pour l'heure et le statut
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        // ========== MODIFIÉ : Confirmation de lecture (doubles ✓✓) ==========
        if (message.isMine()) {
            String status = getMessageStatus(message);
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font(12));
            
            // Changer la couleur selon le statut
            if (status.equals("✓✓")) {
                statusLabel.setStyle("-fx-text-fill: #00a884;"); // Vert pour lu
            } else {
                statusLabel.setStyle("-fx-text-fill: #34B7F1;"); // Bleu pour envoyé
            }
            
            footerBox.getChildren().add(statusLabel);
        }
        
        footerBox.getChildren().add(hourLabel);
        messageBubble.getChildren().addAll(contentLabel, footerBox);
        
        // Style de la bulle
        if (message.isMine()) {
            messageBubble.setStyle(
                "-fx-background-color: " + MY_MESSAGE_BLUE + ";" +
                "-fx-background-radius: 15 15 0 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            mainContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            String bgColor = isPrivateConversation ? OTHER_MESSAGE_COLOR : GROUP_MESSAGE_COLOR;
            messageBubble.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 15 15 15 0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            contentLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
            mainContainer.setAlignment(Pos.CENTER_LEFT);
        }
        
        mainContainer.getChildren().add(messageBubble);
        return mainContainer;
    }
    
    // ========== MODIFIÉ : Extraire le contenu du message fichier avec téléchargement ==========
    private HBox createFileMessageContent(MessageFile message) {
        // Conteneur principal
        HBox mainContainer = new HBox();
        mainContainer.setPadding(new Insets(5, 10, 5, 10));
        
        // Bulle de message
        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(350);
        messageBubble.setPadding(new Insets(10));
        
        // Afficher le nom de l'expéditeur dans les conversations de groupe
        if (!message.isMine() && !isPrivateConversation) {
            Label senderLabel = new Label(message.getSender().getName());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            senderLabel.setStyle("-fx-text-fill: #075E54;");
            messageBubble.getChildren().add(senderLabel);
        }
        
        // Conteneur pour le fichier
        HBox fileBox = new HBox(12);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        // Icône selon l'extension du fichier
        String icon = getFileIcon(message.getFileName());
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));
        
        // Informations du fichier
        VBox fileInfo = new VBox(3);
        
        // Nom du fichier
        Label fileNameLabel = new Label(message.getFileName());
        fileNameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        fileNameLabel.setWrapText(true);
        fileNameLabel.setMaxWidth(200);
        
        // Taille du fichier
        long fileSize = (message.getData() != null) ? message.getData().length : 0;
        Label fileSizeLabel = new Label(formatFileSize(fileSize));
        fileSizeLabel.setFont(Font.font(11));
        fileSizeLabel.setStyle("-fx-text-fill: #667781;");
        
        fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);
        fileBox.getChildren().addAll(iconLabel, fileInfo);
        
        // ========== AMÉLIORÉ : Bouton de téléchargement avec sauvegarde ==========
        Button downloadButton = new Button("⬇ Télécharger");
        downloadButton.setStyle(
            "-fx-background-color: #00a884;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 5 15;" +
            "-fx-cursor: hand;"
        );
        downloadButton.setFont(Font.font(11));
        
        downloadButton.setOnAction(e -> {
            // Ouvrir une boîte de dialogue pour choisir où sauvegarder
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier");
            fileChooser.setInitialFileName(message.getFileName());
            
            // Ajouter des filtres
            String extension = "";
            int lastDot = message.getFileName().lastIndexOf('.');
            if (lastDot > 0) {
                extension = message.getFileName().substring(lastDot + 1);
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers " + extension.toUpperCase(), "*." + extension)
                );
            }
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
            
            File saveFile = fileChooser.showSaveDialog(getScene().getWindow());
            
            if (saveFile != null) {
                try {
                    // Sauvegarder le fichier
                    Files.write(saveFile.toPath(), message.getData());
                    
                    // Afficher une notification de succès
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Téléchargement réussi");
                    alert.setHeaderText(null);
                    alert.setContentText("Fichier enregistré : " + saveFile.getName());
                    alert.showAndWait();
                    
                } catch (Exception ex) {
                    // Afficher une erreur
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de sauvegarder le fichier : " + ex.getMessage());
                    alert.showAndWait();
                    ex.printStackTrace();
                }
            }
        });
        
        // Heure formatée
        String timeStr = message.getTimestamp().format(TIME_FORMATTER);
        
        // Pied du message
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        // ========== MODIFIÉ : Confirmation de lecture pour les fichiers ==========
        if (message.isMine()) {
            String status = getMessageStatus(message);
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font(12));
            
            if (status.equals("✓✓")) {
                statusLabel.setStyle("-fx-text-fill: #00a884;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #34B7F1;");
            }
            
            footerBox.getChildren().add(statusLabel);
        }
        
        Label hourLabel = new Label(timeStr);
        hourLabel.setFont(Font.font(10));
        hourLabel.setStyle("-fx-text-fill: #667781;");
        footerBox.getChildren().add(hourLabel);
        
        messageBubble.getChildren().addAll(fileBox, downloadButton, footerBox);
        
        // Style de la bulle
        if (message.isMine()) {
            messageBubble.setStyle(
                "-fx-background-color: #e3f2fd;" +
                "-fx-background-radius: 15 15 0 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            mainContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            String bgColor = isPrivateConversation ? "#f5f5f5" : "#e8f0fe";
            messageBubble.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 15 15 15 0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            mainContainer.setAlignment(Pos.CENTER_LEFT);
        }
        
        mainContainer.getChildren().add(messageBubble);
        return mainContainer;
    }
    
    // ========== NOUVEAU : Déterminer le statut du message ==========
    // ========== CORRIGÉ : Déterminer le statut du message ==========
   private String getMessageStatus(Message message) {
    // Calculer la différence en minutes entre maintenant et l'heure du message
    long minutesDiff = java.time.Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes();
    
    if (minutesDiff > 1) {
        return "✓✓"; // Lu (si vieux de plus d'1 minute)
    } else {
        return "✓"; // Envoyé
    }

    }
    
    /**
     * Retourne l'icône appropriée selon l'extension du fichier
     */
    private String getFileIcon(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "📄";
        
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        
        switch (extension) {
            case "pdf": return "📕";
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": return "🖼️";
            case "mp3": case "wav": case "ogg": case "flac": return "🎵";
            case "mp4": case "avi": case "mov": case "mkv": return "🎬";
            case "zip": case "rar": case "7z": return "📦";
            case "doc": case "docx": return "📝";
            case "xls": case "xlsx": return "📊";
            case "ppt": case "pptx": return "📽️";
            case "txt": return "📃";
            default: return "📄";
        }
    }
    
    /**
     * Formate la taille du fichier en unités lisibles
     */
    private String formatFileSize(long size) {
        if (size < 0) return "Taille inconnue";
        if (size == 0) return "Fichier vide";
        if (size < 1024) return size + " o";
        if (size < 1024 * 1024) return String.format("%.1f Ko", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f Mo", size / (1024.0 * 1024.0));
        return String.format("%.2f Go", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Définit si la conversation est privée
     */
    public void setPrivateConversation(boolean isPrivate) {
        this.isPrivateConversation = isPrivate;
    }
}