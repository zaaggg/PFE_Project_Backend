// Full ReportController with conditional logic: implemented/homologation skips rest
package com.PFE.DTT.controller;

import com.PFE.DTT.model.*;
import com.PFE.DTT.dto.ReportRequest;
import com.PFE.DTT.repository.*;
import com.PFE.DTT.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProtocolRepository protocolRepository;
    @Autowired private StandardControlCriteriaRepository standardControlCriteriaRepository;
    @Autowired private SpecificControlCriteriaRepository specificControlCriteriaRepository;
    @Autowired private StandardReportEntryRepository standardReportEntryRepository;
    @Autowired private SpecificReportEntryRepository specificReportEntryRepository;
    @Autowired private MaintenanceFormRepository maintenanceFormRepository;
    @Autowired private JwtUtil jwtUtil;

    private User getAuthenticatedUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) throw new RuntimeException("Unauthorized: Token missing");
        int userId = jwtUtil.extractUserId(token.substring(7));
        return userRepository.findById((long) userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request, HttpServletRequest httpRequest) {
        User creator = getAuthenticatedUser(httpRequest);
        if (creator.getRole() != User.Role.DEPARTMENT_MANAGER)
            return ResponseEntity.status(403).body("Only department managers can create reports.");

        Protocol protocol = protocolRepository.findById(request.getProtocolId())
                .orElseThrow(() -> new RuntimeException("Protocol not found"));

        Report report = new Report();
        report.setProtocol(protocol);
        report.setCreatedBy(creator);
        report.setCreatedAt(LocalDateTime.now());
        report.setCompleted(false);
        report.setType(request.getType());
        report.setSerialNumber(request.getSerialNumber());
        report.setEquipmentDescription(request.getEquipmentDescription());
        report.setDesignation(request.getDesignation());
        report.setManufacturer(request.getManufacturer());
        report.setImmobilization(request.getImmobilization());
        report.setServiceSeg(request.getServiceSeg());
        report.setBusinessUnit(request.getBusinessUnit());

        List<ReportUser> reportUsers = request.getDepartmentUserMap().entrySet().stream()
                .map(entry -> {
                    User user = userRepository.findById(entry.getValue())
                            .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                    return new ReportUser(report, user);
                }).collect(Collectors.toList());
        report.setReportUsers(reportUsers);

        List<StandardControlCriteria> standardCriteria = standardControlCriteriaRepository.findAll();
        List<StandardReportEntry> standardEntries = new ArrayList<>();
        for (StandardControlCriteria crit : standardCriteria) {
            StandardReportEntry entry = new StandardReportEntry();
            entry.setReport(report);
            entry.setStandardControlCriteria(crit);
            standardEntries.add(entry);
        }
        report.setReportEntries(standardEntries);

        List<SpecificControlCriteria> specificCriteria = specificControlCriteriaRepository.findByProtocolId(protocol.getId());
        List<SpecificReportEntry> specificEntries = new ArrayList<>();
        for (SpecificControlCriteria crit : specificCriteria) {
            SpecificReportEntry entry = new SpecificReportEntry();
            entry.setReport(report);
            entry.setSpecificControlCriteria(crit);
            specificEntries.add(entry);
        }

        MaintenanceForm form = new MaintenanceForm();
        form.setReport(report);
        report.setMaintenanceForm(form);

        reportRepository.save(report);

        return ResponseEntity.ok("Report created successfully.");
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedReports(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        List<Report> reports = reportRepository.findByReportUsersUserId(user.getId());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportDetails(@PathVariable int id, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        boolean authorized = report.getCreatedBy().getId().equals(user.getId()) ||
                report.getReportUsers().stream().anyMatch(ru -> ru.getUser().getId().equals(user.getId()));
        if (!authorized)
            return ResponseEntity.status(403).body("You are not authorized to view this report.");

        return ResponseEntity.ok(report);
    }

    @PutMapping("/fill-standard-entry/{entryId}")
    public ResponseEntity<?> fillStandardEntry(@PathVariable int entryId, @RequestBody StandardReportEntry updatedEntry, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        StandardReportEntry entry = standardReportEntryRepository.findById(entryId).orElseThrow();

        Department implDept = entry.getStandardControlCriteria().getImplementationResponsible();
        Department checkDept = entry.getStandardControlCriteria().getCheckResponsible();

        if (!user.getDepartment().equals(implDept) && !user.getDepartment().equals(checkDept))
            return ResponseEntity.status(403).body("You are not assigned to this entry.");

        if (entry.getImplemented() != null)
            return ResponseEntity.status(409).body("This entry has already been filled.");

        if (Boolean.TRUE.equals(updatedEntry.getImplemented())) {
            entry.setImplemented(true);
        } else {
            entry.setImplemented(false);
            entry.setAction(updatedEntry.getAction());
            entry.setResponsableAction(updatedEntry.getResponsableAction());
            entry.setDeadline(updatedEntry.getDeadline());
            entry.setSuccessControl(updatedEntry.getSuccessControl());
        }

        standardReportEntryRepository.save(entry);
        return ResponseEntity.ok("Standard entry filled successfully.");
    }

    @PutMapping("/fill-specific-entry/{entryId}")
    public ResponseEntity<?> fillSpecificEntry(@PathVariable int entryId, @RequestBody SpecificReportEntry updatedEntry, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        SpecificReportEntry entry = specificReportEntryRepository.findById(entryId).orElseThrow();

        Set<Department> implDepts = entry.getSpecificControlCriteria().getImplementationResponsibles();
        Set<Department> checkDepts = entry.getSpecificControlCriteria().getCheckResponsibles();

        boolean authorized = implDepts.contains(user.getDepartment()) || checkDepts.contains(user.getDepartment());
        if (!authorized)
            return ResponseEntity.status(403).body("You are not assigned to this entry.");

        if (entry.getHomologation() != null)
            return ResponseEntity.status(409).body("This entry has already been filled.");

        if (Boolean.TRUE.equals(updatedEntry.getHomologation())) {
            entry.setHomologation(true);
        } else {
            entry.setHomologation(false);
            entry.setAction(updatedEntry.getAction());
            entry.setResponsableAction(updatedEntry.getResponsableAction());
            entry.setDeadline(updatedEntry.getDeadline());
            entry.setSuccessControl(updatedEntry.getSuccessControl());
        }

        specificReportEntryRepository.save(entry);
        return ResponseEntity.ok("Specific entry filled successfully.");
    }

    @PutMapping("/fill-maintenance-form/{reportId}")
    public ResponseEntity<?> fillMaintenanceForm(@PathVariable int reportId, @RequestBody MaintenanceForm updatedForm, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        Report report = reportRepository.findById(reportId).orElseThrow();
        MaintenanceForm form = report.getMaintenanceForm();

        if (form == null)
            return ResponseEntity.badRequest().body("No maintenance form found for this report.");

        boolean isMaintenanceUser = user.getDepartment().getName().equalsIgnoreCase("Maintenance system");
        if (!isMaintenanceUser)
            return ResponseEntity.status(403).body("Only users from 'Maintenance system' can fill this form.");

        if (updatedForm.getIsInOrder() != null && (form.getControlCircuit() == null || form.getPowerCircuit() == null))
            return ResponseEntity.badRequest().body("Cannot update 'isInOrder' before other fields are completed.");

        form.setControlCircuit(updatedForm.getControlCircuit());
        form.setPowerCircuit(updatedForm.getPowerCircuit());
        form.setFuseValue(updatedForm.getFuseValue());
        form.setFrequency(updatedForm.getFrequency());
        form.setInsulationResistanceCable(updatedForm.getInsulationResistanceCable());
        form.setInsulationResistanceMotor(updatedForm.getInsulationResistanceMotor());
        form.setPhaseBalanceTest210v(updatedForm.getPhaseBalanceTest210v());
        form.setPhaseBalanceTest380v(updatedForm.getPhaseBalanceTest380v());
        form.setMachineSizeHeight(updatedForm.getMachineSizeHeight());
        form.setMachineSizeLength(updatedForm.getMachineSizeLength());
        form.setMachineSizeWidth(updatedForm.getMachineSizeWidth());
        form.setCurrentType(updatedForm.getCurrentType());
        form.setNetworkForm(updatedForm.getNetworkForm());
        form.setControlStandard(updatedForm.getControlStandard());
        form.setHasTransformer(updatedForm.getHasTransformer());

        if (updatedForm.getIsInOrder() != null)
            form.setIsInOrder(updatedForm.getIsInOrder());

        maintenanceFormRepository.save(form);
        return ResponseEntity.ok("Maintenance form updated successfully.");
    }
}
