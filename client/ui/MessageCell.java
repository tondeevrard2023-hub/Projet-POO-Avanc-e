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
import model.Message;
import model.MessageFile;
import model.MessageText;

public class MessageCell extends ListCell<Message> {

    // Indique si la conversation est privée (pour adapter l'affichage)
    private boolean isPrivateConversation = false;
    
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
            return;
        }
        
        // Récupérer le type de conversation depuis les propriétés de la liste
        Object typeProp = getListView().getProperties().get("conversationType");
        if (typeProp instanceof Boolean) {
            this.isPrivateConversation = (Boolean) typeProp;
        }
        
        if (message instanceof MessageText) {
            showTextMessage((MessageText) message);
        } else if (message instanceof MessageFile) {
            showFileMessage((MessageFile) message);
        } else {
            setText("Message inconnu");
        }
    }
    
    private void showTextMessage(MessageText message) {
        // Conteneur principal pour l'alignement
        HBox mainContainer = new HBox();
        mainContainer.setPadding(new Insets(5, 10, 5, 10));
        
        // Bulle de message
        VBox messageBubble = new VBox(3);
        messageBubble.setMaxWidth(350);
        
        // Afficher le nom de l'expéditeur seulement dans les conversations de groupe
        // et si ce n'est pas mon message
        if (!message.isMine() && !isPrivateConversation) {
            Label senderLabel = new Label(message.getSender().getName());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            senderLabel.setStyle("-fx-text-fill: #075E54;"); // Vert foncé
            senderLabel.setPadding(new Insets(0, 0, 0, 8));
            messageBubble.getChildren().add(senderLabel);
        }
        
        // Contenu du message
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(300);
        contentLabel.setPadding(new Insets(8, 12, 8, 12));
        contentLabel.setFont(Font.font(14));
        
        // Heure
        String timeStr = String.format("%02d:%02d", 
            message.getTimestamp().getHour(), 
            message.getTimestamp().getMinute());
        Label hourLabel = new Label(timeStr);
        hourLabel.setFont(Font.font(10));
        hourLabel.setStyle("-fx-text-fill: #667781;");
        hourLabel.setPadding(new Insets(0, 4, 2, 0));
        
        // Conteneur pour l'heure (et statut si c'est mon message)
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (message.isMine()) {
            Label statusLabel = new Label("✓");
            statusLabel.setFont(Font.font(12));
            statusLabel.setStyle("-fx-text-fill: #34B7F1;"); // Bleu WhatsApp
            footerBox.getChildren().add(statusLabel);
        }
        
        footerBox.getChildren().add(hourLabel);
        
        messageBubble.getChildren().addAll(contentLabel, footerBox);
        
        // Style de la bulle selon l'expéditeur et le type de conversation
        if (message.isMine()) {
            // Mes messages - Style bleu
            messageBubble.setStyle(
                "-fx-background-color: " + MY_MESSAGE_BLUE + ";" +
                "-fx-background-radius: 15 15 0 15;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
            );
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            mainContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // Messages des autres - couleur différente selon le type de conversation
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
        setGraphic(mainContainer);
    }
    
    private void showFileMessage(MessageFile message) {
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
    
    // Icône selon l'extension du fichier - CORRIGÉ
    String icon = getFileIcon(message.getFileName());
    Label iconLabel = new Label(icon);
    iconLabel.setFont(Font.font(24)); // Simple taille 24
    
    // Informations du fichier
    VBox fileInfo = new VBox(3);
    
    // Nom du fichier - CORRIGÉ
    Label fileNameLabel = new Label(message.getFileName());
    fileNameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
    fileNameLabel.setWrapText(true);
    fileNameLabel.setMaxWidth(200);
    
    // Taille du fichier - CORRIGÉ
    long fileSize = (message.getData() != null) ? message.getData().length : 0;
    Label fileSizeLabel = new Label(formatFileSize(fileSize));
    fileSizeLabel.setFont(Font.font(11)); // Simple taille 11
    fileSizeLabel.setStyle("-fx-text-fill: #667781;");
    
    fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);
    fileBox.getChildren().addAll(iconLabel, fileInfo);
    
    // Bouton de téléchargement
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
        System.out.println("Téléchargement de: " + message.getFileName());
        // TODO: Implémenter le téléchargement
    });
    
    // Heure
    String timeStr = String.format("%02d:%02d", 
        message.getTimestamp().getHour(), 
        message.getTimestamp().getMinute());
    
    // Pied du message
    HBox footerBox = new HBox(5);
    footerBox.setAlignment(Pos.CENTER_RIGHT);
    
    if (message.isMine()) {
        Label statusLabel = new Label("✓");
        statusLabel.setFont(Font.font(12));
        statusLabel.setStyle("-fx-text-fill: #34B7F1;");
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
    setGraphic(mainContainer);
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
     * Définit si la conversation est privée (appelé par la ListView)
     */
    public void setPrivateConversation(boolean isPrivate) {
        this.isPrivateConversation = isPrivate;
    }
}