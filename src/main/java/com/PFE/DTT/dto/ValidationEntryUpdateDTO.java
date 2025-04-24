package com.PFE.DTT.dto;

import java.time.LocalDate;

public class ValidationEntryUpdateDTO {
    private Boolean status;
    private String reason;
    private LocalDate date;

    // Getters and Setters
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
}
