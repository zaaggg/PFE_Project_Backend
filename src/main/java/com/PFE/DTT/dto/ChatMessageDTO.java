package com.PFE.DTT.dto;

import com.PFE.DTT.model.MessageStatus;

public class ChatMessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
    private String timestamp;
    private boolean seen;
    private String imageUrl;
    private MessageStatus status;
    private UserDTO sender; // ✅ Optional full sender info (for UI)

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
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public UserDTO getSender() { return sender; }
    public void setSender(UserDTO sender) { this.sender = sender; }
}
