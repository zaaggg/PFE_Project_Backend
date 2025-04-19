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
    private boolean canEditMaintenance;
    private boolean canEditShe;
}
