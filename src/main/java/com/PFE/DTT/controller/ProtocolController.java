package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ProtocolDTO;
import com.PFE.DTT.model.Protocol;
import com.PFE.DTT.model.ProtocolType;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.ProtocolRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil; // ✅ Utility class to extract user ID from token
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    @Autowired
    private ProtocolRepository protocolRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // ✅ Inject JWT Utility

    @GetMapping("/grouped")
    public ResponseEntity<?> getProtocolsGroupedByType(@AuthenticationPrincipal User user) {
        if (user.getRole() != User.Role.DEPARTMENT_MANAGER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        List<ProtocolDTO> protocolDTOs = protocolRepository.findAll().stream()
                .map(ProtocolDTO::new)
                .collect(Collectors.toList());

        Map<ProtocolType, List<ProtocolDTO>> grouped = protocolDTOs.stream()
                .collect(Collectors.groupingBy(ProtocolDTO::getProtocolType));

        return ResponseEntity.ok(grouped);
    }



    // ✅ Create a new Protocol (Only Admins)
    @PostMapping("/create")
    public ResponseEntity<?> createProtocol(
            @RequestBody ProtocolRequest requestBody,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized: Missing or invalid token.");
        }

        int userId = jwtUtil.extractUserId(token.substring(7));
        Optional<User> user = userRepository.findById((long) userId);
        if (user.isEmpty() || user.get().getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("Only admins can create protocols.");
        }

        ProtocolType protocolType;
        try {
            protocolType = ProtocolType.valueOf(requestBody.getProtocolType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid protocol type. Allowed values: HOMOLOGATION, REQUALIFICATION.");
        }

        Protocol protocol = new Protocol(requestBody.getName(), protocolType, user.get());
        protocolRepository.save(protocol);

        return ResponseEntity.ok("Protocol created successfully.");
    }

    static class ProtocolRequest {
        private String name;
        private String protocolType;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProtocolType() { return protocolType; }
        public void setProtocolType(String protocolType) { this.protocolType = protocolType; }
    }
}
