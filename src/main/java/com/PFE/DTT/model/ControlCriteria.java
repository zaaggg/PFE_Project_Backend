package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class ControlCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String description;

    // Responsible Department Type for Implementation
    @ManyToOne
    @JoinColumn(name = "implementation_responsible", nullable = false)
    private DepartmentType implementationResponsible;

    // Responsible Department Type for Checking
    @ManyToOne
    @JoinColumn(name = "check_responsible", nullable = false)
    private DepartmentType checkResponsible;

    // Each ControlCriteria belongs to one Protocol
    @ManyToOne
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    // Constructors
    public ControlCriteria() {}

    public ControlCriteria(String description, DepartmentType implementationResponsible, DepartmentType checkResponsible, Protocol protocol) {
        this.description = description;
        this.implementationResponsible = implementationResponsible;
        this.checkResponsible = checkResponsible;
        this.protocol = protocol;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DepartmentType getImplementationResponsible() {
        return implementationResponsible;
    }

    public void setImplementationResponsible(DepartmentType implementationResponsible) {
        this.implementationResponsible = implementationResponsible;
    }

    public DepartmentType getCheckResponsible() {
        return checkResponsible;
    }

    public void setCheckResponsible(DepartmentType checkResponsible) {
        this.checkResponsible = checkResponsible;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "ControlCriteria{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", implementationResponsible=" + implementationResponsible.getName() +
                ", checkResponsible=" + checkResponsible.getName() +
                ", protocol=" + protocol.getName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ControlCriteria that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
