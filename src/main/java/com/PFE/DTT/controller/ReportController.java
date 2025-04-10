package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ReportCreationRequest;
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
    public ResponseEntity<?> createReport(@RequestBody ReportCreationRequest request, @AuthenticationPrincipal User currentUser){

        if (currentUser.getRole() != User.Role.DEPARTMENT_MANAGER) {
            return ResponseEntity.status(403).body("Only department managers can create reports.");
        }

        Optional<Protocol> optionalProtocol = protocolRepository.findById(request.getProtocolId());
        if (optionalProtocol.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid protocol ID.");
        }

        Protocol protocol = optionalProtocol.get();

        // STEP 1: Collect all unique required department IDs
        Set<Integer> requiredDepartmentIds = new HashSet<>();

        // All standard control criteria in DB
        List<StandardControlCriteria> allStandardCriteria = standardControlCriteriaRepository.findAll();
        for (StandardControlCriteria sc : allStandardCriteria) {
            requiredDepartmentIds.add(sc.getImplementationResponsible().getId());
            requiredDepartmentIds.add(sc.getCheckResponsible().getId());
        }

        // All specific control criteria in selected protocol
        Set<Integer> specificDepartmentIds = new HashSet<>();
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            for (Department d : spc.getImplementationResponsibles()) {
                specificDepartmentIds.add(d.getId());
            }
            for (Department d : spc.getCheckResponsibles()) {
                specificDepartmentIds.add(d.getId());
            }
        }

        requiredDepartmentIds.addAll(specificDepartmentIds);

        // STEP 2: Validate user assignment
        HashMap<Object, Object> departmentToUserMap = new HashMap<>();
        for (UserAssignmentDTO ua : request.getAssignedUsers()) {
            departmentToUserMap.put(ua.getDepartmentId(), ua.getUserId());
        }

        if (!departmentToUserMap.keySet().containsAll(requiredDepartmentIds)) {
            return ResponseEntity.badRequest().body("A user must be assigned for each required department.");
        }

        // STEP 3: Create and populate Report
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

        reportRepository.save(report);

        // STEP 4: Create empty standard report entries
        for (StandardControlCriteria sc : allStandardCriteria) {
            StandardReportEntry entry = new StandardReportEntry();
            entry.setReport(report);
            entry.setStandardControlCriteria(sc);
            entry.setImplemented(false); // ✅ Add this line to fix the error
            entry.setAction("");
            entry.setResponsableAction("");
            entry.setDeadline("");
            entry.setSuccessControl("");
            standardReportEntryRepository.save(entry);

        }

        // STEP 5: Create empty specific report entries
        for (SpecificControlCriteria spc : protocol.getSpecificControlCriteria()) {
            SpecificReportEntry entry = new SpecificReportEntry();
            entry.setReport(report);
            entry.setSpecificControlCriteria(spc);
            entry.setHomologation(false);
            entry.setAction("");
            entry.setResponsableAction("");
            entry.setDeadline("");
            entry.setSuccessControl("");
            specificReportEntryRepository.save(entry);
        }

        // STEP 6: Create empty maintenance form
        MaintenanceForm form = new MaintenanceForm();

        form.setReport(report);

        form.setControlStandard(null);
        form.setCurrentType(null);
        form.setNetworkForm(null);

        form.setPowerCircuit("");
        form.setControlCircuit("");
        form.setFuseValue("");

        form.setHasTransformer(false); // required
        form.setFrequency("");
        form.setPhaseBalanceTest380v("");
        form.setPhaseBalanceTest210v("");
        form.setInsulationResistanceMotor("");
        form.setInsulationResistanceCable("");

        form.setMachineSizeHeight("");
        form.setMachineSizeLength("");
        form.setMachineSizeWidth("");

        form.setIsInOrder(false); // required

        maintenanceFormRepository.save(form);


        return ResponseEntity.ok("Report created successfully.");
    }
}
