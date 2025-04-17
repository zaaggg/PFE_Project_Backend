package com.PFE.DTT.dto;

import com.PFE.DTT.model.Department;

import java.util.List;

public class SpecificChecklistItemDTO {
    private int criteriaId;
    private String criteriaDescription;
    private List<Department> checkResponsibles;
    private List<Department> implementationResponsibles;
    private boolean homologation;
    private String action;
    private String responsableAction;
    private String deadline;
    private String successControl;
    private boolean isUpdated;

    // Getters and Setters
    public int getCriteriaId() { return criteriaId; }
    public void setCriteriaId(int criteriaId) { this.criteriaId = criteriaId; }

    public String getCriteriaDescription() { return criteriaDescription; }
    public void setCriteriaDescription(String criteriaDescription) { this.criteriaDescription = criteriaDescription; }

    public List<Department> getCheckResponsibles() { return checkResponsibles; }
    public void setCheckResponsibles(List<Department> checkResponsibles) { this.checkResponsibles = checkResponsibles; }

    public List<Department> getImplementationResponsibles() { return implementationResponsibles; }
    public void setImplementationResponsibles(List<Department> implementationResponsibles) { this.implementationResponsibles = implementationResponsibles; }

    public boolean isHomologation() { return homologation; }
    public void setHomologation(boolean homologation) { this.homologation = homologation; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResponsableAction() { return responsableAction; }
    public void setResponsableAction(String responsableAction) { this.responsableAction = responsableAction; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getSuccessControl() { return successControl; }
    public void setSuccessControl(String successControl) { this.successControl = successControl; }

    public boolean isUpdated() { return isUpdated; }
    public void setUpdated(boolean updated) { isUpdated = updated; }
}

