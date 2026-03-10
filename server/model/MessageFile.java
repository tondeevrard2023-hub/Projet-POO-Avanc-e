package model;

public class MessageFile extends Message {
    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private byte[] data;
    // private Object content;

    public MessageFile() {
        super();
    }
    public MessageFile(byte[] data) {
        super();
        this.data = data;
    }
    public MessageFile(String fileName, byte[] data) {
        super();
        this.fileName = fileName;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
    // public Object getContent() {
    //     return content;
    // }
    // public void setContent(Object content) {
    //     this.content = content;
    // }
}