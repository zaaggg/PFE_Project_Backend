package com.PFE.DTT.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportMetadataDTO {
    private String type;
    private String serialNumber;
    private String equipmentDescription;
    private String designation;
    private String manufacturer;
    private String immobilization;
    private String serviceSeg;
    private String businessUnit;
    private boolean canEditImmobilization;
}
