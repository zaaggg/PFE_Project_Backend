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

// ReportController.java (updated checklist GET APIs + maintenance form flags)



    @GetMapping("/standard-checklist/{reportId}")
    public ResponseEntity<?> getStandardChecklist(@PathVariable int reportId, @AuthenticationPrincipal User user) {
        List<StandardChecklistItemDTO> checklist = standardReportEntryService.getChecklistForUser((long) reportId, user);
        return ResponseEntity.ok(checklist);
    }


    @GetMapping("/specific-checklist/{reportId}")
    public ResponseEntity<?> getSpecificChecklist(@PathVariable int reportId, @AuthenticationPrincipal User user) {
        List<SpecificChecklistItemDTO> checklist = specificReportEntryService.getChecklistForUser((long) reportId, user);
        return ResponseEntity.ok(checklist);
    }


    // ✅ PUT - Update Standard Checklist Entry
    @PutMapping("/entry/standard/batch-update")
    public ResponseEntity<Map<String, String>> updateMultipleStandardEntries(@RequestBody List<StandardReportEntryDTO> entries,
                                                                             @AuthenticationPrincipal User user) {
        for (StandardReportEntryDTO dto : entries) {
            String result = standardReportEntryService.updateEntry(dto.getId(), dto, user);
            if (!"OK".equals(result)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to update entry ID " + dto.getId() + ": " + result);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "Selected standard entries updated successfully");
        return ResponseEntity.ok(successResponse);
    }




    // ✅ PUT - Update Specific Checklist Entry
    @PutMapping("/entry/specific/batch-update")
    public ResponseEntity<?> updateMultipleSpecificEntries(@RequestBody List<SpecificReportEntryDTO> entries,
                                                           @AuthenticationPrincipal User user) {
        for (SpecificReportEntryDTO dto : entries) {
            String result = specificReportEntryService.updateEntry(dto.getId(), dto, user);
            if (!"OK".equals(result)) {
                return ResponseEntity.badRequest().body("Failed to update entry ID " + dto.getId() + ": " + result);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Selected specific entries updated successfully"));
    }




    @GetMapping("/maintenance-form/{reportId}")
    public ResponseEntity<?> getMaintenanceForm(@PathVariable int reportId, @AuthenticationPrincipal User user) {
        Optional<MaintenanceForm> formOpt = maintenanceFormRepository.findByReportId(reportId);
        if (formOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Maintenance form not found");
        }

        MaintenanceForm form = formOpt.get();
        String department = user.getDepartment().getName().trim().toLowerCase();

        boolean canEditMaintenance = "maintenance system".equals(department)
                && Boolean.FALSE.equals(form.getMaintenanceSystemUpdated());

        boolean canEditShe = "she".equals(department)
                && Boolean.TRUE.equals(form.getMaintenanceSystemUpdated())
                && Boolean.FALSE.equals(form.getSheUpdated());

        MaintenanceFormDTO dto = new MaintenanceFormDTO(form, canEditMaintenance, canEditShe);

        System.out.println("Sending DTO: " + dto); // ✅ Confirm in logs

        return ResponseEntity.ok(dto);
    }







    @PutMapping("/maintenance-form/update/{reportId}")
    public ResponseEntity<?> updateMaintenanceForm(
            @PathVariable int reportId,
            @RequestBody MaintenanceForm updatedForm,
            @AuthenticationPrincipal User user) {

        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Report not found"));
        }

        Optional<MaintenanceForm> formOpt = maintenanceFormRepository.findByReportId(reportId);
        if (formOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Maintenance form not found"));
        }

        Report report = reportOpt.get();
        MaintenanceForm form = formOpt.get();

        boolean isAssigned = report.getAssignedUsers().contains(user);
        boolean isCreator = report.getCreatedBy().getId().equals(user.getId());
        String department = user.getDepartment().getName().trim().toLowerCase();

        // MAINTENANCE SYSTEM
        if ("maintenance system".equals(department)) {
            if (!isAssigned && !isCreator) {
                return ResponseEntity.status(403).body(Map.of("error", "Not allowed: Only assigned users or creator can update"));
            }

            if (Boolean.TRUE.equals(form.getMaintenanceSystemUpdated())) {
                return ResponseEntity.status(403).body(Map.of("error", "Already filled by maintenance system"));
            }

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

            form.setMaintenanceSystemUpdated(true);
            maintenanceFormRepository.save(form);

            return ResponseEntity.ok(Map.of("message", "Maintenance system part updated"));
        }

        // SHE
        if ("she".equals(department)) {
            if (!Boolean.TRUE.equals(form.getMaintenanceSystemUpdated())) {
                return ResponseEntity.status(403).body(Map.of("error", "Maintenance system must complete their section first"));
            }

            if (!isAssigned && !isCreator) {
                return ResponseEntity.status(403).body(Map.of("error", "Not allowed: Only assigned users or creator can update"));
            }

            if (Boolean.TRUE.equals(form.getSheUpdated())) {
                return ResponseEntity.status(403).body(Map.of("error", "Already filled by SHE"));
            }

            form.setIsInOrder(updatedForm.getIsInOrder());
            form.setSheUpdated(true);
            maintenanceFormRepository.save(form);

            return ResponseEntity.ok(Map.of("message", "SHE part updated"));
        }

        return ResponseEntity.status(403).body(Map.of("error", "You are not authorized to update any part of the maintenance form"));
    }



}
