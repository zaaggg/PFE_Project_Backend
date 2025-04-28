package com.PFE.DTT.dto;


import java.time.LocalDate;

public class StandardReportEntryDTO {
    private int id;
    private boolean implemented;
    private String action;
    private String responsableAction;
    private LocalDate deadline;
    private String successControl;
    private boolean isUpdated;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isImplemented() { return implemented; }
    public void setImplemented(boolean implemented) { this.implemented = implemented; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResponsableAction() { return responsableAction; }
    public void setResponsableAction(String responsableAction) { this.responsableAction = responsableAction; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getSuccessControl() { return successControl; }
    public void setSuccessControl(String successControl) { this.successControl = successControl; }

    public boolean isUpdated() { return isUpdated; }
    public void setUpdated(boolean updated) { isUpdated = updated; }
}