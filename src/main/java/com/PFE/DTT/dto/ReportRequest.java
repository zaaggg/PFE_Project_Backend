package com.PFE.DTT.dto;

import java.util.Map;

public class ReportRequest {
    private int protocolId;
    private Map<Integer, Long> departmentUserMap;
    private String type;
    private String serialNumber;
    private String equipmentDescription;
    private String designation;
    private String manufacturer;
    private String immobilization;
    private String serviceSeg;
    private String businessUnit;

    public int getProtocolId() { return protocolId; }
    public Map<Integer, Long> getDepartmentUserMap() { return departmentUserMap; }
    public String getType() { return type; }
    public String getSerialNumber() { return serialNumber; }
    public String getEquipmentDescription() { return equipmentDescription; }
    public String getDesignation() { return designation; }
    public String getManufacturer() { return manufacturer; }
    public String getImmobilization() { return immobilization; }
    public String getServiceSeg() { return serviceSeg; }
    public String getBusinessUnit() { return businessUnit; }
}

