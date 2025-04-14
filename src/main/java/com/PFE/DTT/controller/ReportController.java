package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ReportCreationRequest;
import com.PFE.DTT.dto.StandardReportEntryUpdateRequest;
import com.PFE.DTT.dto.SpecificReportEntryUpdateRequest;
import com.PFE.DTT.dto.UserAssignmentDTO;
import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/rapports")
public class ReportController {

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StandardControlCriteriaRepository standardControlCriteriaRepository;

    @Autowired
    private SpecificControlCriteriaRepository specificControlCriteriaRepository;

    @Autowired
    private StandardReportEntryRepository standardReportEntryRepository;

    @Autowired
    private SpecificReportEntryRepository specificReportEntryRepository;

    @Autowired
    private MaintenanceFormRepository maintenanceFormRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody ReportCreationRequest request,
                                          @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() != User.Role.DEPARTMENT_MANAGER) {
            return ResponseEntity.status(403).body("Only department managers can create reports.");
        }

        Optional<Protocol> optionalProtocol = protocolRepository.findById(request.getProtocolId());
        if (optionalProtocol.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid protocol ID.");
        }
        Protocol protocol = optionalProtocol.get();

        // Collect required department IDs
        Set<Integer> requiredDepartmentIds = new HashSet<>();
        List<StandardControlCriteria> allStandardCriteria = standardControlCriteriaRepository.findAll();
        for (StandardControlCriteria sc : allStandardCriteria) {
            requiredDepartmentIds.add(sc.getImplementationResponsible().getId());
            requiredDepartmentIds.add(sc.getCheckResponsible().getId());
        }
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            spc.getImplementationResponsibles().forEach(d -> requiredDepartmentIds.add(d.getId()));
            spc.getCheckResponsibles().forEach(d -> requiredDepartmentIds.add(d.getId()));
        }

        // Validate user assignments
        Map<Integer, Integer> departmentToUserMap = new HashMap<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            departmentToUserMap.put(ua.getDepartmentId(), (int) ua.getUserId());
        }

        if (!departmentToUserMap.keySet().containsAll(requiredDepartmentIds)) {
            return ResponseEntity.badRequest().body("A user must be assigned for each required department.");
        }

        // Create report and set fields
        Report report = new Report();
        report.setProtocol(protocol);
        report.setCreatedBy(currentUser);
        report.setCreatedAt(LocalDateTime.now());
        report.setIsCompleted(false);
        report.setType(request.getType());
        report.setSerialNumber(request.getSerialNumber());
        report.setEquipmentDescription(request.getEquipmentDescription());
        report.setDesignation(request.getDesignation());
        report.setManufacturer(request.getManufacturer());
        report.setImmobilization(request.getImmobilization());
        report.setServiceSeg(request.getServiceSeg());
        report.setBusinessUnit(request.getBusinessUnit());

// Ensure the list is initialized


// ✅ Use a regular loop instead of lambda to avoid 'effectively final' issue
        Set<User> assignedUsers = new HashSet<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            userRepository.findById(ua.getUserId()).ifPresent(assignedUsers::add);
        }
        report.setAssignedUsers(assignedUsers);


// ✅ Save the report once with cascade
        Report savedReport = reportRepository.save(report);



        // Create standard report entries
        for (StandardControlCriteria sc : allStandardCriteria) {
            StandardReportEntry entry = new StandardReportEntry();
            entry.setReport(savedReport);
            entry.setStandardControlCriteria(sc);
            entry.setImplemented(false);
            entry.setAction("");
            entry.setResponsableAction("");
            entry.setDeadline("");
            entry.setSuccessControl("");
            entry.setUpdated(false);
            standardReportEntryRepository.save(entry);
        }

        // Create specific report entries
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            SpecificReportEntry entry = new SpecificReportEntry();
            entry.setReport(savedReport);
            entry.setSpecificControlCriteria(spc);
            entry.setHomologation(false);
            entry.setAction("");
            entry.setResponsableAction("");
            entry.setDeadline("");
            entry.setSuccessControl("");
            entry.setUpdated(false);
            specificReportEntryRepository.save(entry);
        }

        // Create maintenance form
        MaintenanceForm form = new MaintenanceForm();
        form.setReport(savedReport);
        form.setControlStandard(null);
        form.setCurrentType(null);
        form.setNetworkForm(null);
        form.setPowerCircuit("");
        form.setControlCircuit("");
        form.setFuseValue("");
        form.setHasTransformer(false);
        form.setFrequency("");
        form.setPhaseBalanceTest380v("");
        form.setPhaseBalanceTest210v("");
        form.setInsulationResistanceMotor("");
        form.setInsulationResistanceCable("");
        form.setMachineSizeHeight("");
        form.setMachineSizeLength("");
        form.setMachineSizeWidth("");
        form.setIsInOrder(false);
        maintenanceFormRepository.save(form);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Report created successfully.");
        return ResponseEntity.ok(response);
    }




    @PutMapping("/entry/specific/{entryId}")
    public ResponseEntity<?> updateSpecificEntry(
            @PathVariable int entryId,
            @RequestBody SpecificReportEntryUpdateRequest req,
            @AuthenticationPrincipal User user
    ) {
        Optional<SpecificReportEntry> optionalEntry = specificReportEntryRepository.findById(entryId);
        if (optionalEntry.isEmpty()) {
            return ResponseEntity.badRequest().body("Entry not found");
        }

        SpecificReportEntry entry = optionalEntry.get();

        if (entry.isUpdated()) {
            return ResponseEntity.badRequest().body("This entry has already been updated.");
        }

        SpecificControlCriteria criteria = entry.getSpecificControlCriteria();

        boolean isAssignedUser = entry.getReport().getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
        boolean isCheckDept = criteria.getCheckResponsibles().stream()
                .anyMatch(dep -> dep.getId() == (user.getDepartment().getId()));

        if (!isAssignedUser || !isCheckDept) {
            return ResponseEntity.status(403).body("You are not authorized to fill this entry");
        }

        if (req.getHomologation() == null) {
            return ResponseEntity.badRequest().body("Homologation field is required");
        }

        entry.setHomologation(req.getHomologation());
        if (req.getHomologation()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            if (req.getAction() == null || req.getResponsableAction() == null ||
                    req.getDeadline() == null || req.getSuccessControl() == null) {
                return ResponseEntity.badRequest().body("All fields are required when homologation is false");
            }
            entry.setAction(req.getAction());
            entry.setResponsableAction(req.getResponsableAction());
            entry.setDeadline(req.getDeadline());
            entry.setSuccessControl(req.getSuccessControl());
        }

        entry.setUpdated(true);
        specificReportEntryRepository.save(entry);
        return ResponseEntity.ok("Specific entry updated successfully");
    }
    @PutMapping("/entry/standard/{entryId}")
    public ResponseEntity<?> updateStandardEntry(
            @PathVariable int entryId,
            @RequestBody StandardReportEntryUpdateRequest req,
            @AuthenticationPrincipal User user
    ) {
        Optional<StandardReportEntry> optionalEntry = standardReportEntryRepository.findById(entryId);
        if (optionalEntry.isEmpty()) {
            return ResponseEntity.badRequest().body("Entry not found");
        }

        StandardReportEntry entry = optionalEntry.get();

        if (entry.isUpdated()) {
            return ResponseEntity.badRequest().body("This entry has already been updated.");
        }

        StandardControlCriteria criteria = entry.getStandardControlCriteria();

        boolean isAssignedUser = entry.getReport().getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
        boolean isCheckDept = criteria.getCheckResponsible().getId() == (user.getDepartment().getId());

        if (!isAssignedUser || !isCheckDept) {
            return ResponseEntity.status(403).body("You are not authorized to fill this entry");
        }

        if (req.getImplemented() == null) {
            return ResponseEntity.badRequest().body("Implemented field is required");
        }

        entry.setImplemented(req.getImplemented());
        if (req.getImplemented()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            if (req.getAction() == null || req.getResponsableAction() == null ||
                    req.getDeadline() == null || req.getSuccessControl() == null) {
                return ResponseEntity.badRequest().body("All fields are required when implemented is false");
            }
            entry.setAction(req.getAction());
            entry.setResponsableAction(req.getResponsableAction());
            entry.setDeadline(req.getDeadline());
            entry.setSuccessControl(req.getSuccessControl());
        }

        entry.setUpdated(true);
        standardReportEntryRepository.save(entry);
        return ResponseEntity.ok("Standard entry updated successfully");
    }


    @PutMapping("/maintenance-form/{reportId}/fill")
    public ResponseEntity<?> fillMaintenanceForm(
            @PathVariable int reportId,
            @RequestBody MaintenanceForm updatedForm,
            @AuthenticationPrincipal User user
    ) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) return ResponseEntity.badRequest().body("Report not found");

        Report report = reportOpt.get();

        Optional<MaintenanceForm> formOpt = maintenanceFormRepository.findByReportId(reportId);
        if (formOpt.isEmpty()) return ResponseEntity.badRequest().body("Maintenance form not found");

        MaintenanceForm form = formOpt.get();

        boolean isAssigned = report.getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (!isAssigned) {
            return ResponseEntity.status(403).body("You are not assigned to this report");
        }

        String departmentName = user.getDepartment().getName().trim().toLowerCase();

        if ("maintenance system".equals(departmentName)) {
            form.setControlStandard(updatedForm.getControlStandard());
            form.setCurrentType(updatedForm.getCurrentType());
            form.setNetworkForm(updatedForm.getNetworkForm());
            form.setPowerCircuit(updatedForm.getPowerCircuit());
            form.setControlCircuit(updatedForm.getControlCircuit());
            form.setFuseValue(updatedForm.getFuseValue());
            form.setHasTransformer(updatedForm.getHasTransformer());
            form.setFrequency(updatedForm.getFrequency());
            form.setPhaseBalanceTest380v(updatedForm.getPhaseBalanceTest380v());
            form.setPhaseBalanceTest210v(updatedForm.getPhaseBalanceTest210v());
            form.setInsulationResistanceMotor(updatedForm.getInsulationResistanceMotor());
            form.setInsulationResistanceCable(updatedForm.getInsulationResistanceCable());
            form.setMachineSizeHeight(updatedForm.getMachineSizeHeight());
            form.setMachineSizeLength(updatedForm.getMachineSizeLength());
            form.setMachineSizeWidth(updatedForm.getMachineSizeWidth());

            maintenanceFormRepository.save(form);
            return ResponseEntity.ok("Form details filled except isInOrder");
        }

        if ("she".equals(departmentName)) {
            form.setIsInOrder(updatedForm.getIsInOrder());
            maintenanceFormRepository.save(form);
            return ResponseEntity.ok("isInOrder field updated");
        }

        return ResponseEntity.status(403).body("You do not have permission to update the maintenance form");
    }

    @GetMapping("/my-created")
    public ResponseEntity<?> getReportsCreatedByMe(@AuthenticationPrincipal User user) {
        if (user.getRole() != User.Role.DEPARTMENT_MANAGER) {
            return ResponseEntity.status(403).body("Unauthorized: Only department managers can view created reports");
        }
        List<Report> reports = reportRepository.findByCreatedBy(user.getId());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getReportsAssignedToMe(@AuthenticationPrincipal User user) {
        List<Report> reports = reportRepository.findAssignedToUser(user.getId());
        return ResponseEntity.ok(reports);
    }
}
