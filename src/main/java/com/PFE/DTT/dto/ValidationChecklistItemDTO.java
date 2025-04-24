package com.PFE.DTT.dto;

import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.ProtocolType;
import com.PFE.DTT.model.ValidationEntry;

import java.time.LocalDate;

public class ValidationChecklistItemDTO {

    private Long id;
    private String criteria;
    private Department department;       // ✅ Full department object
    private ProtocolType protocolType;   // ✅ Enum type
    private Boolean status;
    private String reason;
    private LocalDate date;
    private Boolean updated;

    // ✅ Constructor
    public ValidationChecklistItemDTO(ValidationEntry entry) {
        this.id = entry.getId();
        this.criteria = entry.getReportValidation().getCriteria();
        this.department = entry.getReportValidation().getResponsibleDepartment();
        this.protocolType = entry.getReportValidation().getProtocolType();
        this.status = entry.getStatus();
        this.reason = entry.getReason();
        this.date = entry.getDate();
        this.updated = entry.   getUpdated();
    }

    // ✅ Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }
}
