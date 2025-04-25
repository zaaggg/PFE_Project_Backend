package com.PFE.DTT.dto;

import com.PFE.DTT.model.Department;

import java.util.List;

public class SpecificCriteriaDTO {
    private String description;
    private List<Department> implementationResponsibles;

    private List<Department> checkResponsibles;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public List<Department> getImplementationResponsibles() {
        return implementationResponsibles;
    }

    public void setImplementationResponsibles(List<Department> implementationResponsibles) {
        this.implementationResponsibles = implementationResponsibles;
    }

    public List<Department> getCheckResponsibles() {
        return checkResponsibles;
    }

    public void setCheckResponsibles(List<Department> checkResponsibles) {
        this.checkResponsibles = checkResponsibles;
    }
}
