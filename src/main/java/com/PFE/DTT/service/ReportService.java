package com.PFE.DTT.service;

import com.PFE.DTT.model.Report;
import com.PFE.DTT.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void updateReportCompletionStatus(Long reportId) {
        boolean allStandardUpdated = standardReportEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.getIsUpdated()));

        boolean allSpecificUpdated = specificReportEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.isUpdated()));

        boolean maintenanceSystemDone = maintenanceFormRepository
                .isMaintenanceSystemPartFilled(reportId); // you implement this custom query

        boolean maintenanceSheDone = maintenanceFormRepository
                .isShePartFilled(reportId); // you implement this custom query

        boolean allValidationUpdated = validationEntryRepository
                .findByReportId(reportId)
                .stream().allMatch(e -> Boolean.TRUE.equals(e.getUpdated()));

        boolean isCompleted = allStandardUpdated && allSpecificUpdated && maintenanceSystemDone && maintenanceSheDone && allValidationUpdated;

        reportRepository.findById(Math.toIntExact(reportId)).ifPresent(report -> {
            report.setIsCompleted(isCompleted);
            reportRepository.save(report);
        });
    }
}
