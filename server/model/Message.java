package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private User sender;
    private LocalDateTime timestamp;
    private boolean isMine;
    private String visibility;

    public Message() {
        this.timestamp = LocalDateTime.now();
        this.sender = new User();
        this.visibility = "public";
    }

    public Message(User sender) {
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
        this.visibility = "public";
    }
    
    public User getSender() {
        return sender;
    }
    public void setSender(User sender) {
        this.sender = sender;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public boolean isMine() {
        return isMine;
    }
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}