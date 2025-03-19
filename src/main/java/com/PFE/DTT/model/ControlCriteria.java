package com.PFE.DTT.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Abstract base class representing a control criteria entity in the system.
 * Defines common attributes for all control criteria types, including departments responsible for implementation and checking.
 * Used as a MappedSuperclass to share fields with SpecificControlCriteria and StandardControlCriteria.
 */
@MappedSuperclass
public abstract class ControlCriteria {

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "implementation_responsible", nullable = false)
    private Department implementationResponsible;

    @ManyToOne
    @JoinColumn(name = "check_responsible", nullable = false)
    private Department checkResponsible;

    // Default Constructor (JPA Requirement)
    protected ControlCriteria() {}

    // Constructor
    protected ControlCriteria(String description, Department implementationResponsible, Department checkResponsible) {
        this.description = description;
        this.implementationResponsible = implementationResponsible;
        this.checkResponsible = checkResponsible;
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Department getImplementationResponsible() {
        return implementationResponsible;
    }

    public void setImplementationResponsible(Department implementationResponsible) {
        this.implementationResponsible = implementationResponsible;
    }

    public Department getCheckResponsible() {
        return checkResponsible;
    }

    public void setCheckResponsible(Department checkResponsible) {
        this.checkResponsible = checkResponsible;
    }

    @Override
    public String toString() {
        return "ControlCriteria{" +
                "description='" + description + '\'' +
                ", implementationResponsible=" + (implementationResponsible != null ? implementationResponsible.getId() : "null") +
                ", checkResponsible=" + (checkResponsible != null ? checkResponsible.getId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ControlCriteria that)) return false;
        return Objects.equals(description, that.description) &&
                Objects.equals(implementationResponsible, that.implementationResponsible) &&
                Objects.equals(checkResponsible, that.checkResponsible);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, implementationResponsible, checkResponsible);
    }
}