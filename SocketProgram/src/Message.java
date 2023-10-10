public class Message {
    private String sender;
    private String message;
    private boolean isRead;
    private int messageId;

    public Message(String sender, String message, boolean isRead, int messageId) {
        this.sender = sender;
        this.message = message;
        this.isRead = isRead;
        this.messageId = messageId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getMessageId() {
        return messageId;
    }
}