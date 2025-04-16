package com.PFE.DTT.controller;

import com.PFE.DTT.dto.*;
import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.*;
import com.PFE.DTT.service.SpecificReportEntryService;
import com.PFE.DTT.service.StandardReportEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rapports")
public class ReportController {

    @Autowired private ProtocolRepository protocolRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private StandardControlCriteriaRepository standardControlCriteriaRepository;
    @Autowired private SpecificControlCriteriaRepository specificControlCriteriaRepository;
    @Autowired private StandardReportEntryRepository standardReportEntryRepository;
    @Autowired private SpecificReportEntryRepository specificReportEntryRepository;
    @Autowired private MaintenanceFormRepository maintenanceFormRepository;
    @Autowired private StandardReportEntryService standardReportEntryService;
    @Autowired private SpecificReportEntryService specificReportEntryService;

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

        Map<Integer, Integer> departmentToUserMap = new HashMap<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            departmentToUserMap.put(ua.getDepartmentId(), (int) ua.getUserId());
        }

        if (!departmentToUserMap.keySet().containsAll(requiredDepartmentIds)) {
            return ResponseEntity.badRequest().body("A user must be assigned for each required department.");
        }

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

        Set<User> assignedUsers = new HashSet<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            userRepository.findById(ua.getUserId()).ifPresent(assignedUsers::add);
        }
        report.setAssignedUsers(assignedUsers);

        Report savedReport = reportRepository.save(report);

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

        MaintenanceForm form = new MaintenanceForm();
        form.setReport(savedReport);
        maintenanceFormRepository.save(form);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Report created successfully.");
        return ResponseEntity.ok(response);
    }

    private ReportDTO mapToDTO(Report report) {
        Set<AssignedUserDTO> assignedUserDTOs = report.getAssignedUsers().stream()
                .map(user -> new AssignedUserDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getProfilePhoto(),
                        user.getDepartment(),
                        user.getPlant()
                ))
                .collect(Collectors.toSet());

        return new ReportDTO(
                report.getId(),
                report.getType(),
                report.getSerialNumber(),
                report.getEquipmentDescription(),
                report.getDesignation(),
                report.getManufacturer(),
                report.getImmobilization(),
                report.getServiceSeg(),
                report.getBusinessUnit(),
                report.getCreatedAt(),
                report.getCreatedBy(),
                assignedUserDTOs
        );
    }

    @PutMapping("/entry/standard/{entryId}")
    public ResponseEntity<?> updateStandardEntry(@PathVariable int entryId,
                                                 @RequestBody StandardReportEntryUpdateRequest req,
                                                 @AuthenticationPrincipal User user) {
        return standardReportEntryService.updateEntry(entryId, req, user);
    }


    @PutMapping("/entry/specific/{entryId}")
    public ResponseEntity<?> updateSpecificEntry(@PathVariable int entryId,
                                                 @RequestBody SpecificReportEntryUpdateRequest req,
                                                 @AuthenticationPrincipal User user) {
        return specificReportEntryService.updateEntry(entryId, req, user);
    }

    @PutMapping("/maintenance-form/{reportId}")
    public ResponseEntity<?> fillMaintenanceForm(@PathVariable int reportId,
                                                 @RequestBody MaintenanceForm updatedForm,
                                                 @AuthenticationPrincipal User user) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) return ResponseEntity.badRequest().body("Report not found");

        Report report = reportOpt.get();
        Optional<MaintenanceForm> formOpt = maintenanceFormRepository.findByReportId(reportId);
        if (formOpt.isEmpty()) return ResponseEntity.badRequest().body("Maintenance form not found");

        MaintenanceForm form = formOpt.get();
        boolean isAssigned = report.getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
        if (!isAssigned) return ResponseEntity.status(403).body("You are not assigned to this report");

        String dept = user.getDepartment().getName().trim().toLowerCase();
        if ("maintenance system".equals(dept)) {
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

        if ("she".equals(dept)) {
            form.setIsInOrder(updatedForm.getIsInOrder());
            maintenanceFormRepository.save(form);
            return ResponseEntity.ok("isInOrder field updated");
        }

        return ResponseEntity.status(403).body("You do not have permission to update the maintenance form");
    }

    @GetMapping("/my-created")
    public ResponseEntity<?> getReportsCreatedByMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("User not authenticated");
        if (user.getRole() != User.Role.DEPARTMENT_MANAGER) return ResponseEntity.status(403).body("Unauthorized");
        List<Report> reports = reportRepository.findByCreatedBy(user.getId());
        List<ReportDTO> reportDTOs = reports.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getReportsAssignedToMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        List<Report> reports = reportRepository.findAssignedToUser(user.getId());
        List<ReportDTO> reportDTOs = reports.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(reportDTOs);
    }
}
