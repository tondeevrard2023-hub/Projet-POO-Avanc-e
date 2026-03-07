package model;
import java.time.LocalDateTime; 
public class MessageText extends Message {
    private static final long serialVersionUID = 1L;
    
    private String content;

    public MessageText() {
        super();
    }
    public MessageText(String content) {
        super();
        this.content = content;
    }
    public MessageText(String content, User sender) {
        super(sender);
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}