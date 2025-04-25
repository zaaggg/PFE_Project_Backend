package com.PFE.DTT.dto;

import com.PFE.DTT.model.ProtocolType;

import java.util.List;

public class ProtocolCreationRequest {
    private String name;
    private ProtocolType protocolType;
    private List<SpecificCriteriaDTO> specificCriteria;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public List<SpecificCriteriaDTO> getSpecificCriteria() {
        return specificCriteria;
    }

    public void setSpecificCriteria(List<SpecificCriteriaDTO> specificCriteria) {
        this.specificCriteria = specificCriteria;
    }
}

