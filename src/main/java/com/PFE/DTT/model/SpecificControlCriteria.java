package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.*;

/**
 * Represents a specific control criteria entity, tailored for a particular protocol
 * with associated departments for implementation and checking.
 */
@Entity
@Table(name = "specific_control_criteria")
public class SpecificControlCriteria extends ControlCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    // Default Constructor (JPA Requirement)
    public SpecificControlCriteria() {
        super();
    }

    // Constructor
    public SpecificControlCriteria(String description, Department implementationResponsible, Department checkResponsible, Protocol protocol) {
        super(description, implementationResponsible, checkResponsible);
        this.protocol = protocol;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "SpecificControlCriteria{" +
                "id=" + id +
                ", protocol=" + (protocol != null ? protocol.getName() : "null") +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof SpecificControlCriteria that)) return false;
        return id == that.id && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, protocol);
    }
}