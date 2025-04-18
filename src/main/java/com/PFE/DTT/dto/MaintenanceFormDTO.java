package com.PFE.DTT.dto;
import com.PFE.DTT.model.MaintenanceForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceFormDTO {
    private MaintenanceForm form;
    private Boolean canEditMaintenance;
    private Boolean canEditShe;



    public MaintenanceForm getForm() {
        return form;
    }

    public void setForm(MaintenanceForm form) {
        this.form = form;
    }

    public Boolean getCanEditMaintenance() {
        return canEditMaintenance;
    }

    public void setCanEditMaintenance(Boolean canEditMaintenance) {
        this.canEditMaintenance = canEditMaintenance;
    }

    public Boolean getCanEditShe() {
        return canEditShe;
    }

    public void setCanEditShe(Boolean canEditShe) {
        this.canEditShe = canEditShe;
    }
}
