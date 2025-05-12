package com.PFE.DTT.controller;

import com.PFE.DTT.dto.*;
import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.*;
import com.PFE.DTT.service.EmailService;
import com.PFE.DTT.service.ReportService;
import com.PFE.DTT.service.SpecificReportEntryService;
import com.PFE.DTT.service.StandardReportEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Autowired private StandardControlCriteriaRepository standardControlCriteriaRepository;
    @Autowired private StandardReportEntryRepository standardReportEntryRepository;
    @Autowired private SpecificReportEntryRepository specificReportEntryRepository;
    @Autowired private MaintenanceFormRepository maintenanceFormRepository;
    @Autowired private StandardReportEntryService standardReportEntryService;
    @Autowired private SpecificReportEntryService specificReportEntryService;
    @Autowired ReportValidationRepository reportValidationRepository;
    @Autowired ValidationEntryRepository validationEntryRepository;
    @Autowired
    private ReportService reportService;
    @Autowired
    private EmailService emailService;




    @GetMapping("/required-users/{protocolId}")
    public ResponseEntity<List<UserDTO>> getRequiredUsersForProtocol(@PathVariable Long protocolId) {
        Optional<Protocol> optionalProtocol = protocolRepository.findById(Math.toIntExact(protocolId));
        if (optionalProtocol.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Protocol protocol = optionalProtocol.get();

        Set<Long> departmentIds = new HashSet<>();

        // 1. Standard Control Criteria
        List<StandardControlCriteria> standards = standardControlCriteriaRepository.findAll();
        for (StandardControlCriteria sc : standards) {
            departmentIds.add((long) sc.getCheckResponsible().getId());
            departmentIds.add((long) sc.getImplementationResponsible().getId());
        }

        // 2. Specific Control Criteria (only related to selected protocol)
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            spc.getImplementationResponsibles().forEach(dep -> departmentIds.add((long) dep.getId()));
            spc.getCheckResponsibles().forEach(dep -> departmentIds.add((long) dep.getId()));
        }

        // 3. Report Validations with same protocol type
        List<ReportValidation> validations = reportValidationRepository.findByProtocolType(protocol.getProtocolType());
        for (ReportValidation rv : validations) {
            if (rv.getResponsibleDepartment() != null) {
                departmentIds.add((long) rv.getResponsibleDepartment().getId());
            }
        }

        // 4. Fetch users in those departments
        List<User> users = userRepository.findByDepartmentIdIn(departmentIds);
        List<UserDTO> userDTOs = users.stream().map(UserDTO::fromEntity).collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }




    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody ReportCreationRequest request,
                                          @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() != User.Role.DEPARTMENT_MANAGER && currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("Only department managers or admin can create reports.");
        }

        Optional<Protocol> optionalProtocol = protocolRepository.findById(request.getProtocolId());
        if (optionalProtocol.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid protocol ID.");
        }

        Protocol protocol = optionalProtocol.get();

        // ✅ 1. Collect required department IDs
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

        // ✅ 2. Fetch ReportValidation entries with matching protocol type
        List<ReportValidation> matchingValidations =
                reportValidationRepository.findByProtocolType(protocol.getProtocolType());

        for (ReportValidation rv : matchingValidations) {
            if (rv.getResponsibleDepartment() != null) {
                requiredDepartmentIds.add(rv.getResponsibleDepartment().getId());
            }
        }

        // ✅ 3. Check assigned users cover all required departments
        Map<Integer, Integer> departmentToUserMap = new HashMap<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            departmentToUserMap.put(ua.getDepartmentId(), (int) ua.getUserId());
        }

        if (!departmentToUserMap.keySet().containsAll(requiredDepartmentIds)) {
            return ResponseEntity.badRequest().body("A user must be assigned for each required department.");
        }

        // ✅ 4. Create and save the report
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

        // ✅ 5. Save standard entries
        for (StandardControlCriteria sc : allStandardCriteria) {
            StandardReportEntry entry = new StandardReportEntry();
            entry.setReport(savedReport);
            entry.setStandardControlCriteria(sc);
            entry.setImplemented(null);
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
            entry.setUpdated(false);
            standardReportEntryRepository.save(entry);
        }

        // ✅ 6. Save specific entries
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            SpecificReportEntry entry = new SpecificReportEntry();
            entry.setReport(savedReport);
            entry.setSpecificControlCriteria(spc);
            entry.setHomologation(null);
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
            entry.setUpdated(false);
            specificReportEntryRepository.save(entry);
        }

        // ✅ 7. Create and save maintenance form
        MaintenanceForm form = new MaintenanceForm();
        form.setReport(savedReport);
        maintenanceFormRepository.save(form);

        // ✅ 8. Save validation entries based on assigned department

        for (ReportValidation rv : matchingValidations) {
            ValidationEntry ve = new ValidationEntry();
            ve.setReport(savedReport);
            ve.setReportValidation(rv);
            ve.setStatus(null);
            ve.setReason(null);
            ve.setDate(null);
            ve.setUpdated(false);

            validationEntryRepository.save(ve);

            System.out.println("✅ Created ValidationEntry for ReportValidation ID: " + rv.getId());
        }

        // ✅ 9. Notify assigned users by email
// Notify assigned users
        for (User assignedUser : report.getAssignedUsers()) {
            String email = assignedUser.getEmail();
            String protocolName = report.getProtocol().getName();
            String protocolType = String.valueOf(report.getProtocol().getProtocolType());
            String createdByFirstName = report.getCreatedBy().getFirstName();
            String createdByLastName = report.getCreatedBy().getLastName();

            // or report.getType() or any other label

            emailService.sendReportCreationEmail(email, protocolName, protocolType, createdByFirstName , createdByLastName);
            System.out.println("Sending email to " + assignedUser.getEmail());
        }



        Map<String, Object> response = new HashMap<>();
        response.put("message", "Report created successfully.");
        response.put("reportId", savedReport.getId());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/metadata/{reportId}")
    public ResponseEntity<ReportMetadataDTO> getReportMetadata(@PathVariable Long reportId,
                                                               @AuthenticationPrincipal User currentUser) {
        Report report = reportRepository.findById(Math.toIntExact(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found"));

        ReportMetadataDTO dto = reportService.toMetadataDTO(report, currentUser);
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/rapports/update-immobilization/{reportId}")
    public ResponseEntity<?> updateImmobilization(@PathVariable int reportId,
                                                  @RequestBody ImmobilizationUpdateDTO dto,
                                                  @AuthenticationPrincipal User currentUser) {
        Optional<Report> optionalReport = reportRepository.findById(reportId);
        if (optionalReport.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found.");
        }

        Report report = optionalReport.get();

        // Only the creator can edit
        if (!report.getCreatedBy().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this report.");
        }

        // Only if immobilization is empty
        if (report.getImmobilization() != null && !report.getImmobilization().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Immobilization has already been filled.");
        }


        reportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Immobilization updated successfully."));

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

    @GetMapping("/validation-checklist/{reportId}")
    public ResponseEntity<?> getValidationChecklist(@PathVariable Long reportId) {
        Optional<Report> optionalReport = reportRepository.findById(Math.toIntExact(reportId));
        if (optionalReport.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found.");
        }

        List<ValidationEntry> entries = validationEntryRepository.findByReportId(reportId);
        List<ValidationChecklistItemDTO> checklist = entries.stream()
                .map(ValidationChecklistItemDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(checklist);
    }

    @PutMapping("/validation-entry/{entryId}")
    public ResponseEntity<?> updateValidationEntry(
            @PathVariable Long entryId,
            @RequestBody ValidationEntryUpdateDTO dto,
            @AuthenticationPrincipal User user) {

        // 🔍 Log debug information
        System.out.println("🔄 Request to update ValidationEntry ID: " + entryId);
        System.out.println("🔐 Authenticated user: " + user.getEmail());
        System.out.println("📎 User department: " +
                (user.getDepartment() != null ? user.getDepartment().getId() : "null"));

        Optional<ValidationEntry> optionalEntry = validationEntryRepository.findById(entryId);

        if (optionalEntry.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Validation entry not found"));
        }

        ValidationEntry entry = optionalEntry.get();

        if (entry.getReportValidation() == null || entry.getReportValidation().getResponsibleDepartment() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Validation entry is missing responsible department"));
        }

        int responsibleDeptId = entry.getReportValidation().getResponsibleDepartment().getId();
        int userDeptId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        System.out.println("📎 Responsible Dept ID: " + responsibleDeptId);
        System.out.println("📎 User Dept ID: " + userDeptId);

        if ( !(responsibleDeptId == (userDeptId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not authorized to update this validation entry"));
        }

        // ✅ Proceed with update
        entry.setStatus(dto.getStatus());
        entry.setReason(dto.getReason());
        entry.setDate(dto.getDate());
        entry.setUpdated(true);
        validationEntryRepository.save(entry);

        // ✅ Re-check report completion
        reportService.updateReportCompletionStatus(Long.valueOf(entry.getReport().getId()));

        return ResponseEntity.ok(Map.of("message", "Validation entry updated successfully"));
    }


    @GetMapping("/{reportId}/assigned-users")
    public ResponseEntity<List<UserDTO>> getAssignedUsers(@PathVariable Long reportId) {
        Report report = reportRepository.findById(Math.toIntExact(reportId))
                .orElseThrow(() -> new RuntimeException("Report not found"));

        List<UserDTO> assignedUsers = report.getAssignedUsers().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(assignedUsers);
    }


    @GetMapping("/my-created")
    public ResponseEntity<?> getReportsCreatedByMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("User not authenticated");
        if (user.getRole() != User.Role.DEPARTMENT_MANAGER) return ResponseEntity.status(403).body("Unauthorized");

        List<Report> reports = reportRepository.findByCreatedBy(user.getId());

        List<ReportDTO> reportDTOs = reports.stream().map(report -> {
            ReportDTO dto = mapToDTO(report);
            dto.setProgress(reportService.calculateReportProgressPercentage(report)); // ✅ set progress
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(reportDTOs);
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getReportsAssignedToMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");

        List<Report> reports = reportRepository.findAssignedToUser(user.getId());

        List<ReportDTO> reportDTOs = reports.stream().map(report -> {
            ReportDTO dto = mapToDTO(report);
            dto.setProgress(reportService.calculateReportProgressPercentage(report)); // ✅ set progress
            return dto;
        }).collect(Collectors.toList());

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
    public ResponseEntity<Map<String, String>> updateMultipleStandardEntries(
            @RequestBody List<StandardReportEntryDTO> entries,
            @AuthenticationPrincipal User user) {

        Map<String, String> response = new HashMap<>();

        if (entries == null || entries.isEmpty()) {
            response.put("error", "No entries provided.");
            return ResponseEntity.badRequest().body(response);
        }

        Set<Long> updatedReportIds = new HashSet<>();

        for (StandardReportEntryDTO dto : entries) {
            String result = standardReportEntryService.updateEntry(dto.getId(), dto, user);

            if (!"OK".equals(result)) {
                response.put("error", "Failed to update entry ID " + dto.getId() + ": " + result);
                return ResponseEntity.badRequest().body(response);
            }

            Long reportId = Long.valueOf(standardReportEntryService.getReportIdByEntryId(dto.getId()));
            updatedReportIds.add(reportId);
        }

        // ✅ Now check completion status only once per report ID
        for (Long reportId : updatedReportIds) {
            reportService.updateReportCompletionStatus(reportId);
        }

        response.put("message", "Selected standard entries updated successfully.");
        return ResponseEntity.ok(response);
    }





    // ✅ PUT - Update Specific Checklist Entry
    @PutMapping("/entry/specific/batch-update")
    public ResponseEntity<?> updateMultipleSpecificEntries(
            @RequestBody List<SpecificReportEntryDTO> entries,
            @AuthenticationPrincipal User user) {

        if (entries == null || entries.isEmpty()) {
            return ResponseEntity.badRequest().body("No entries provided.");
        }

        Set<Long> updatedReportIds = new HashSet<>();

        for (SpecificReportEntryDTO dto : entries) {
            String result = specificReportEntryService.updateEntry(dto.getId(), dto, user);

            if (!"OK".equals(result)) {
                return ResponseEntity.badRequest().body("Failed to update entry ID " + dto.getId() + ": " + result);
            }

            Long reportId = Long.valueOf(specificReportEntryService.getReportIdByEntryId(dto.getId()));
            updatedReportIds.add(reportId);
        }

        // ✅ Trigger completion check once per report
        for (Long reportId : updatedReportIds) {
            reportService.updateReportCompletionStatus(reportId);
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
            @PathVariable Long reportId,
            @RequestBody MaintenanceForm updatedForm,
            @AuthenticationPrincipal User user) {

        Optional<Report> reportOpt = reportRepository.findById(Math.toIntExact(reportId));
        if (reportOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Report not found"));
        }

        Optional<MaintenanceForm> formOpt = maintenanceFormRepository.findByReportId(Math.toIntExact(reportId));
        if (formOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Maintenance form not found"));
        }

        Report report = reportOpt.get();
        MaintenanceForm form = formOpt.get();

        boolean isAssigned = report.getAssignedUsers().contains(user);
        boolean isCreator = report.getCreatedBy().getId().equals(user.getId());
        String department = user.getDepartment().getName().trim().toLowerCase();

        // ✅ MAINTENANCE SYSTEM
        if ("maintenance system".equals(department)) {
            if (!isAssigned && !isCreator) {
                return ResponseEntity.status(403).body(Map.of("error", "Not allowed: Only assigned users or creator can update"));
            }

            if (Boolean.TRUE.equals(form.getMaintenanceSystemUpdated())) {
                return ResponseEntity.status(403).body(Map.of("error", "Already filled by Maintenance System"));
            }

            // Update fields
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

            reportService.updateReportCompletionStatus(reportId);
            return ResponseEntity.ok(Map.of("message", "Maintenance system part updated"));
        }

        // ✅ SHE
        if ("she".equals(department)) {
            if (!Boolean.TRUE.equals(form.getMaintenanceSystemUpdated())) {
                return ResponseEntity.status(403).body(Map.of("error", "Maintenance System must complete their section first"));
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

            reportService.updateReportCompletionStatus(reportId);
            return ResponseEntity.ok(Map.of("message", "SHE part updated"));
        }

        return ResponseEntity.status(403).body(Map.of("error", "You are not authorized to update any part of the maintenance form"));
    }



    @GetMapping("/test-mail")
    public ResponseEntity<?> testMail() {
        emailService.sendReportCreationEmail("zaagkhalyl@gmail.com", "Test Protocol", "Homologation", "John", "Doe");
        return ResponseEntity.ok("Test email sent");
    }

}