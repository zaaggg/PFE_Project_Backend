package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.*;


/**
 * Represents a standard control criteria entity, defining standard evaluation rules
 * with associated departments for implementation and checking.
 */
@Entity
@Table(name = "standard_control_criteria")
public class StandardControlCriteria extends ControlCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Default Constructor (JPA Requirement)
    public StandardControlCriteria() {
        super();
    }

    // Constructor
    public StandardControlCriteria(String description, Department implementationResponsible, Department checkResponsible) {
        super(description, implementationResponsible, checkResponsible);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "StandardControlCriteria{" +
                "id=" + id +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof StandardControlCriteria that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}