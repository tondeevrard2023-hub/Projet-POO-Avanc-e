package model;

import java.time.LocalDateTime;

import ui.ChatView.ConversationType;

// Classe interne pour représenter un élément de conversation
public class ConversationItem {
    private String id;
    private String name;
    private ConversationType type;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private User otherUser;
    
    public ConversationItem(String id, String name, ConversationType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lastMessage = "";
        this.lastMessageTime = LocalDateTime.now();
        this.unreadCount = 0;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public ConversationType getType() { return type; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void incrementUnreadCount() { this.unreadCount++; }
    public User getOtherUser() { return otherUser; }
    public void setOtherUser(User otherUser) { this.otherUser = otherUser; }
    
    @Override
    public String toString() {
        return name;
    }
}