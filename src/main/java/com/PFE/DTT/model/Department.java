package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ensures ID is just a number
    private int id;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "department_type_id", nullable = false)
    private DepartmentType departmentType;

    // Constructors
    public Department() {}

    public Department(Plant plant, DepartmentType departmentType) {
        this.plant = plant;
        this.departmentType = departmentType;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public DepartmentType getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(DepartmentType departmentType) {
        this.departmentType = departmentType;
    }
}
