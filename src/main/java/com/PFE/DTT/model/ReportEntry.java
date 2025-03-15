package com.PFE.DTT.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "report_entry")
public class ReportEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne
    @JoinColumn(name = "control_criteria_id") // Null si c'est un critère standard
    private ControlCriteria controlCriteria;

    @ManyToOne
    @JoinColumn(name = "standard_criteria_id") // Null si c'est un critère spécifique
    private StandardCriteria standardCriteria;

    private Boolean implemented; // Oui / Non

    private String action; // Action corrective

    private String responsableAction; // Now a simple String instead of a User entity

    private String deadline;

    private String successControl; // Vérification finale



    // Default Constructor
    public ReportEntry() {}

    // Constructor with all fields except ID
    public ReportEntry(Report report, ControlCriteria controlCriteria, StandardCriteria standardCriteria,
                       Boolean implemented, String action, String responsableAction,
                       String deadline, String successControl) {
        this.report = report;
        this.controlCriteria = controlCriteria;
        this.standardCriteria = standardCriteria;
        this.implemented = implemented;
        this.action = action;
        this.responsableAction = responsableAction;
        this.deadline = deadline;
        this.successControl = successControl;

    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public ControlCriteria getControlCriteria() {
        return controlCriteria;
    }

    public void setControlCriteria(ControlCriteria controlCriteria) {
        this.controlCriteria = controlCriteria;
    }

    public StandardCriteria getStandardCriteria() {
        return standardCriteria;
    }

    public void setStandardCriteria(StandardCriteria standardCriteria) {
        this.standardCriteria = standardCriteria;
    }

    public Boolean getImplemented() {
        return implemented;
    }

    public void setImplemented(Boolean implemented) {
        this.implemented = implemented;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResponsableAction() {
        return responsableAction;
    }

    public void setResponsableAction(String responsableAction) {
        this.responsableAction = responsableAction;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getSuccessControl() {
        return successControl;
    }

    public void setSuccessControl(String successControl) {
        this.successControl = successControl;
    }



    // toString() for logging/debugging
    @Override
    public String toString() {
        return "ReportEntry{" +
                "id=" + id +
                ", report=" + (report != null ? report.getId() : "N/A") +
                ", controlCriteria=" + (controlCriteria != null ? controlCriteria.getId() : "N/A") +
                ", standardCriteria=" + (standardCriteria != null ? standardCriteria.getId() : "N/A") +
                ", implemented=" + implemented +
                ", action='" + action + '\'' +
                ", responsableAction='" + responsableAction + '\'' +
                ", deadline='" + deadline + '\'' +
                ", successControl='" + successControl + '\'' +
                '}';
    }

    // Equals & HashCode based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportEntry that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
