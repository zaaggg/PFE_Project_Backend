package com.PFE.DTT.service;

import com.PFE.DTT.dto.ReportMetadataDTO;
import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StandardReportEntryRepository standardReportEntryRepository;

    @Autowired
    private SpecificReportEntryRepository specificReportEntryRepository;

    @Autowired
    private MaintenanceFormRepository maintenanceFormRepository;

    @Autowired
    private ValidationEntryRepository validationEntryRepository;

    @Autowired
    private EmailService emailService;

    public void updateReportCompletionStatus(Long reportId) {
        boolean allStandardUpdated = standardReportEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.getIsUpdated()));

        boolean allSpecificUpdated = specificReportEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.isUpdated()));

        boolean maintenanceSystemDone = maintenanceFormRepository
                .isMaintenanceSystemPartFilled(reportId);

        boolean maintenanceSheDone = maintenanceFormRepository
                .isShePartFilled(reportId);

        boolean allValidationUpdated = validationEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.getUpdated()));

        boolean isCompleted = allStandardUpdated && allSpecificUpdated && maintenanceSystemDone && maintenanceSheDone && allValidationUpdated;

        reportRepository.findById(Math.toIntExact(reportId)).ifPresent(report -> {
            boolean wasAlreadyCompleted = report.isCompleted();

            report.setIsCompleted(isCompleted);
            reportRepository.save(report);

            if (!wasAlreadyCompleted && isCompleted) {
                emailService.sendReportCompletedEmail(
                        report.getCreatedBy().getEmail(),
                        report.getProtocol().getName(),
                        report.getSerialNumber(),
                        report.getCreatedBy().getFirstName(),
                        report.getCreatedBy().getLastName()
                );
            }
        });
    }


    public ReportMetadataDTO toMetadataDTO(Report report, User currentUser) {
        boolean canEdit = (report.getImmobilization() == null || report.getImmobilization().isEmpty())
                && report.getCreatedBy().getId() == currentUser.getId();

        return new ReportMetadataDTO(
                report.getType(),
                report.getSerialNumber(),
                report.getEquipmentDescription(),
                report.getDesignation(),
                report.getManufacturer(),
                report.getImmobilization(),
                report.getServiceSeg(),
                report.getBusinessUnit(),
                canEdit
        );
    }

    public int calculateReportProgressPercentage(Report report) {
        int progress = 0;

        // Standard checklist: 25%
        List<StandardReportEntry> standardEntries = standardReportEntryRepository.findByReportId(Long.valueOf(report.getId()));
        if (!standardEntries.isEmpty()) {
            int updated = (int) standardEntries.stream().filter(e -> Boolean.TRUE.equals(e.getIsUpdated())).count();
            progress += (int) ((updated / (double) standardEntries.size()) * 25);
        }

        // Specific checklist: 25%
        List<SpecificReportEntry> specificEntries = specificReportEntryRepository.findByReportId(Long.valueOf(report.getId()));
        if (!specificEntries.isEmpty()) {
            int updated = (int) specificEntries.stream().filter(e -> Boolean.TRUE.equals(e.isUpdated())).count();
            progress += (int) ((updated / (double) specificEntries.size()) * 25);
        }

        // Validation checklist: 25%
        List<ValidationEntry> validationEntries = validationEntryRepository.findByReportId(Long.valueOf(report.getId()));
        if (!validationEntries.isEmpty()) {
            int updated = (int) validationEntries.stream().filter(e -> Boolean.TRUE.equals(e.getUpdated())).count();
            progress += (int) ((updated / (double) validationEntries.size()) * 25);
        }

        // Maintenance form: 25% split (15% system + 10% SHE)
        if (maintenanceFormRepository.isMaintenanceSystemPartFilled(Long.valueOf(report.getId()))) {
            progress += 15;
        }
        if (maintenanceFormRepository.isShePartFilled(Long.valueOf(report.getId()))) {
            progress += 10;
        }

        return progress;
    }

}
