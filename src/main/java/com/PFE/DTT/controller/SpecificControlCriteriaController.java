package com.PFE.DTT.controller;

import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.SpecificControlCriteriaRepository;
import com.PFE.DTT.repository.DepartmentRepository;
import com.PFE.DTT.repository.ProtocolRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/control-criteria")
public class SpecificControlCriteriaController {

    @Autowired
    private SpecificControlCriteriaRepository controlCriteriaRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createControlCriteria(
            @RequestBody ControlCriteriaRequest requestBody,
            HttpServletRequest request) {

        // ✅ Extract user ID from JWT token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized: Missing or invalid token.");
        }
        int userId = jwtUtil.extractUserId(token.substring(7)); // Remove "Bearer " and extract ID

        // ✅ Find User
        Optional<User> user = userRepository.findById((long) userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(403).body("User not found.");
        }

        // ✅ Find Protocol
        Optional<Protocol> protocol = protocolRepository.findById(requestBody.getProtocolId());
        if (protocol.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid protocol ID.");
        }

        // ✅ Verify the user is the creator of the protocol
        if (!protocol.get().getCreatedBy().getId().equals(user.get().getId())) {
            return ResponseEntity.status(403).body("You are not authorized to add control criteria to this protocol.");
        }

        // ✅ Find Department Types
        Optional<Department> implementationResponsible = departmentRepository.findById(requestBody.getImplementationResponsibleId());
        Optional<Department> checkResponsible = departmentRepository.findById(requestBody.getCheckResponsibleId());

        if (implementationResponsible.isEmpty() || checkResponsible.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid department type IDs.");
        }

        // ✅ Create and Save ControlCriteria
        SpecificControlCriteria controlCriteria = new SpecificControlCriteria(
                requestBody.getDescription(),
                implementationResponsible.get(),
                checkResponsible.get(),
                protocol.get()
        );
        controlCriteriaRepository.save(controlCriteria);

        return ResponseEntity.ok("ControlCriteria created successfully.");
    }

    // DTO Class for JSON Request Body
    static class ControlCriteriaRequest {
        private String description;
        private int implementationResponsibleId;
        private int checkResponsibleId;
        private int protocolId;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getImplementationResponsibleId() { return implementationResponsibleId; }
        public void setImplementationResponsibleId(int implementationResponsibleId) { this.implementationResponsibleId = implementationResponsibleId; }

        public int getCheckResponsibleId() { return checkResponsibleId; }
        public void setCheckResponsibleId(int checkResponsibleId) { this.checkResponsibleId = checkResponsibleId; }

        public int getProtocolId() { return protocolId; }
        public void setProtocolId(int protocolId) { this.protocolId = protocolId; }
    }
}
