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
import java.time.format.DateTimeFormatter;

import model.Message;
import model.MessageFile;
import model.MessageText;

public class MessageCell extends ListCell<Message> {

    private boolean isPrivateConversation = false;
    
    private LocalDate lastMessageDate = null;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            lastMessageDate = null;
            return;
        }
        
        Object typeProp = getListView().getProperties().get("conversationType");
        if (typeProp instanceof Boolean) {
            this.isPrivateConversation = (Boolean) typeProp;
        }
        
        VBox container = new VBox();
        
        LocalDate messageDate = message.getTimestamp().toLocalDate();
        if (lastMessageDate == null || !lastMessageDate.equals(messageDate)) {
            container.getChildren().add(createDateSeparator(messageDate));
            lastMessageDate = messageDate;
        }
        
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
    
    private HBox createDateSeparator(LocalDate date) {
        HBox separator = new HBox();
        separator.setAlignment(Pos.CENTER);
        separator.setPadding(new Insets(10, 0, 10, 0));
        
        Label dateLabel = new Label(date.format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("date-separator");
        
        separator.getChildren().add(dateLabel);
        return separator;
    }
    
    private HBox createTextMessageContent(MessageText message) {
        HBox mainContainer = new HBox();
        mainContainer.setPadding(new Insets(5, 10, 5, 10));
        
        VBox messageBubble = new VBox(3);
        messageBubble.setMaxWidth(350);
        messageBubble.getStyleClass().add(message.isMine() ? "message-bubble-mine" : 
            (isPrivateConversation ? "message-bubble-other-private" : "message-bubble-other-group"));
        
        if (!message.isMine() && !isPrivateConversation) {
            Label senderLabel = new Label(message.getSender().getName());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            senderLabel.getStyleClass().add("message-sender");
            senderLabel.setPadding(new Insets(0, 0, 0, 8));
            messageBubble.getChildren().add(senderLabel);
        }
        
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(300);
        contentLabel.setPadding(new Insets(8, 12, 8, 12));
        contentLabel.setFont(Font.font(14));
        contentLabel.getStyleClass().add(message.isMine() ? "message-text-mine" : "message-text-other");
        
        String timeStr = message.getTimestamp().format(TIME_FORMATTER);
        Label hourLabel = new Label(timeStr);
        hourLabel.setFont(Font.font(10));
        hourLabel.getStyleClass().add("message-time");
        hourLabel.setPadding(new Insets(0, 4, 2, 0));
        
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (message.isMine()) {
            String status = getMessageStatus(message);
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font(12));
            statusLabel.getStyleClass().add(status.equals("✓✓") ? "status-read" : "status-sent");
            footerBox.getChildren().add(statusLabel);
        }
        
        footerBox.getChildren().add(hourLabel);
        messageBubble.getChildren().addAll(contentLabel, footerBox);
        
        mainContainer.setAlignment(message.isMine() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        mainContainer.getChildren().add(messageBubble);
        
        return mainContainer;
    }
    
    private HBox createFileMessageContent(MessageFile message) {
        HBox mainContainer = new HBox();
        mainContainer.setPadding(new Insets(5, 10, 5, 10));
        
        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(350);
        messageBubble.setPadding(new Insets(10));
        messageBubble.getStyleClass().add(message.isMine() ? "file-message-mine" : 
            (isPrivateConversation ? "file-message-other-private" : "file-message-other-group"));
        
        if (!message.isMine() && !isPrivateConversation) {
            Label senderLabel = new Label(message.getSender().getName());
            senderLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            senderLabel.getStyleClass().add("message-sender");
            messageBubble.getChildren().add(senderLabel);
        }
        
        HBox fileBox = new HBox(12);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        String icon = getFileIcon(message.getFileName());
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(24));
        
        VBox fileInfo = new VBox(3);
        
        Label fileNameLabel = new Label(message.getFileName());
        fileNameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        fileNameLabel.setWrapText(true);
        fileNameLabel.setMaxWidth(200);
        
        long fileSize = (message.getData() != null) ? message.getData().length : 0;
        Label fileSizeLabel = new Label(formatFileSize(fileSize));
        fileSizeLabel.setFont(Font.font(11));
        fileSizeLabel.getStyleClass().add("file-size");
        
        fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);
        fileBox.getChildren().addAll(iconLabel, fileInfo);
        
        Button downloadButton = new Button("⬇ Télécharger");
        downloadButton.getStyleClass().add("download-button");
        downloadButton.setFont(Font.font(11));
        
        downloadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier");
            fileChooser.setInitialFileName(message.getFileName());
            
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
                    Files.write(saveFile.toPath(), message.getData());
                    
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Téléchargement réussi");
                    alert.setHeaderText(null);
                    alert.setContentText("Fichier enregistré : " + saveFile.getName());
                    alert.showAndWait();
                    
                } catch (Exception ex) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de sauvegarder le fichier : " + ex.getMessage());
                    alert.showAndWait();
                    ex.printStackTrace();
                }
            }
        });
        
        String timeStr = message.getTimestamp().format(TIME_FORMATTER);
        
        HBox footerBox = new HBox(5);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (message.isMine()) {
            String status = getMessageStatus(message);
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font(12));
            statusLabel.getStyleClass().add(status.equals("✓✓") ? "status-read" : "status-sent");
            footerBox.getChildren().add(statusLabel);
        }
        
        Label hourLabel = new Label(timeStr);
        hourLabel.setFont(Font.font(10));
        hourLabel.getStyleClass().add("message-time");
        footerBox.getChildren().add(hourLabel);
        
        messageBubble.getChildren().addAll(fileBox, downloadButton, footerBox);
        
        mainContainer.setAlignment(message.isMine() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        mainContainer.getChildren().add(messageBubble);
        
        return mainContainer;
    }
    
    private String getMessageStatus(Message message) {
        long minutesDiff = java.time.Duration.between(message.getTimestamp(), LocalDateTime.now()).toMinutes();
        return minutesDiff > 1 ? "✓✓" : "✓";
    }
    
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
    
    private String formatFileSize(long size) {
        if (size < 0) return "Taille inconnue";
        if (size == 0) return "Fichier vide";
        if (size < 1024) return size + " o";
        if (size < 1024 * 1024) return String.format("%.1f Ko", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f Mo", size / (1024.0 * 1024.0));
        return String.format("%.2f Go", size / (1024.0 * 1024.0 * 1024.0));
    }
}