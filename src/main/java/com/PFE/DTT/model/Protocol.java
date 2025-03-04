package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    // Each Protocol belongs to one ProtocolType
    @ManyToOne
    @JoinColumn(name = "protocol_type_id", nullable = false)
    private ProtocolType protocolType;

    // Protocol is created by an Admin user
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Each Protocol can have many ControlCriteria
    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ControlCriteria> controlCriteriaList = new ArrayList<>();

    // Constructors
    public Protocol() {}

    public Protocol(String name, ProtocolType protocolType, User createdBy) {
        this.name = name;
        this.protocolType = protocolType;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<ControlCriteria> getControlCriteriaList() {
        return controlCriteriaList;
    }

    public void setControlCriteriaList(List<ControlCriteria> controlCriteriaList) {
        this.controlCriteriaList = controlCriteriaList;
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", protocolType=" + protocolType.getName() +
                ", createdBy=" + createdBy.getEmail() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Protocol that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
