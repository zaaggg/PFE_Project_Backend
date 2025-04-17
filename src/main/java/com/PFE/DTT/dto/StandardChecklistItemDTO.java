package com.PFE.DTT.dto;

import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.StandardReportEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardChecklistItemDTO {
    private int criteriaId;
    private String criteriaDescription;
    private Department checkResponsible;
    private Department implementationResponsible;
    private boolean implemented;
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

    public Department getCheckResponsible() { return checkResponsible; }
    public void setCheckResponsible(Department checkResponsible) { this.checkResponsible = checkResponsible; }

    public Department getImplementationResponsible() { return implementationResponsible; }
    public void setImplementationResponsible(Department implementationResponsible) { this.implementationResponsible = implementationResponsible; }

    public boolean isImplemented() { return implemented; }
    public void setImplemented(boolean implemented) { this.implemented = implemented; }

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
