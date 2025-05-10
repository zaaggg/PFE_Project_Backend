package com.PFE.DTT.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private String link;

    private LocalDateTime time;


    private boolean seen  = false;

    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;



    // Constructors
    public Notification() {}

    public Notification(String description, String link, LocalDateTime time, boolean isSeen, User user) {
        this.description = description;
        this.link = link;
        this.time = time;
        this.seen = isSeen;
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public boolean isSeen() {
        return seen;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
