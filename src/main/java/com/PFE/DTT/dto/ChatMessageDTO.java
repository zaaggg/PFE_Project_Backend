package com.PFE.DTT.dto;

public class ChatMessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
    private String timestamp; // date-time of sending
    private boolean seen;
    private String imageUrl; // optional, for Cloudinary uploads

    // Constructors
    public ChatMessageDTO() {}

    public ChatMessageDTO(Long senderId, Long receiverId, String content, String timestamp, boolean seen, String imageUrl) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.seen = seen;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
