package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "standard_criteria") // Optional but recommended
public class StandardCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "implementation_responsible", nullable = false)
    private DepartmentType implementationResponsible;

    @ManyToOne
    @JoinColumn(name = "check_responsible", nullable = false)
    private DepartmentType checkResponsible;

    // Default Constructor (JPA Requirement)
    public StandardCriteria() {}

    // Constructor
    public StandardCriteria(String description, DepartmentType implementationResponsible, DepartmentType checkResponsible) {
        this.description = description;
        this.implementationResponsible = implementationResponsible;
        this.checkResponsible = checkResponsible;
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

    @Override
    public String toString() {
        return "StandardCriteria{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", implementationResponsible=" + (implementationResponsible != null ? implementationResponsible.getName() : "N/A") +
                ", checkResponsible=" + (checkResponsible != null ? checkResponsible.getName() : "N/A") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandardCriteria that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
