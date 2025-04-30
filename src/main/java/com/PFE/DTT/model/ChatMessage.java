package com.PFE.DTT.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User sender;

    private String content;

    // Text content (nullable if image)
    private String imageUrl; // Image URL in Cloudinary (nullable if text)

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageStatus status; // SENT, DELIVERED, SEEN
}
