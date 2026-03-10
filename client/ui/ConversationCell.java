// Cellule personnalisée pour les conversations
package ui;

import java.time.LocalDateTime;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.ConversationItem;
import ui.ChatView.ConversationType;

public class ConversationCell extends ListCell<ConversationItem> {
    @Override
    protected void updateItem(ConversationItem item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            HBox hbox = new HBox(10);
            hbox.setPadding(new Insets(8, 10, 8, 10));
            hbox.setAlignment(Pos.CENTER_LEFT);
            
            if (isSelected()) {
                hbox.getStyleClass().add("conversation-item-selected");
            }
            
            Label avatarLabel = new Label();
            avatarLabel.setMinSize(40, 40);
            avatarLabel.setMaxSize(40, 40);
            avatarLabel.setAlignment(Pos.CENTER);
            avatarLabel.getStyleClass().add(item.getType() == 
                ConversationType.PUBLIC_GROUP ? "avatar-group" : "avatar-private");
            
            if (item.getType() == ConversationType.PUBLIC_GROUP) {
                avatarLabel.setText("👥");
            } else {
                avatarLabel.setText("👤");
            }
            
            VBox infoBox = new VBox(3);
            
            Label nameLabel = new Label(item.getName());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            Label lastMessageLabel = new Label(item.getLastMessage());
            lastMessageLabel.setFont(Font.font(12));
            lastMessageLabel.getStyleClass().add("conversation-last-message");
            lastMessageLabel.setMaxWidth(150);
            
            infoBox.getChildren().addAll(nameLabel, lastMessageLabel);
            
            VBox rightBox = new VBox(5);
            rightBox.setAlignment(Pos.TOP_RIGHT);
            
            Label timeLabel = new Label(formatTime(item.getLastMessageTime()));
            timeLabel.setFont(Font.font(11));
            timeLabel.getStyleClass().add("conversation-time");
            
            Label unreadLabel = new Label();
            if (item.getUnreadCount() > 0) {
                unreadLabel.setText(String.valueOf(item.getUnreadCount()));
                unreadLabel.getStyleClass().add("unread-badge");
            }
            
            rightBox.getChildren().addAll(timeLabel, unreadLabel);
            
            hbox.getChildren().addAll(avatarLabel, infoBox, rightBox);
            HBox.setHgrow(infoBox, Priority.ALWAYS);
            
            setGraphic(hbox);
        }
    }
    
    private String formatTime(LocalDateTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}