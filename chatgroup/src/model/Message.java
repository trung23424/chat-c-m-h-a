package model;

import java.time.LocalDateTime;

public class Message {
    private String senderName;
    private String content;
    private LocalDateTime sentAt;

    public Message(String senderName, String content, LocalDateTime sentAt) {
        this.senderName = senderName;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
