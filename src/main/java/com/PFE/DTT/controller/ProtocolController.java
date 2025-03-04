package com.PFE.DTT.controller;

import com.PFE.DTT.model.Protocol;
import com.PFE.DTT.model.ProtocolType;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.ProtocolRepository;
import com.PFE.DTT.repository.ProtocolTypeRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil; // ✅ Utility class to extract user ID from token
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/protocols")
public class ProtocolController {

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private ProtocolTypeRepository protocolTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // ✅ Inject JWT Utility

    // ✅ Create a new Protocol (Only Admins)
    @PostMapping("/create")
    public ResponseEntity<?> createProtocol(
            @RequestBody ProtocolRequest requestBody,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized: Missing or invalid token.");
        }

        int userId = jwtUtil.extractUserId(token.substring(7)); // Remove "Bearer " and extract ID
        Optional<User> user = userRepository.findById((long) userId);
        if (user.isEmpty() || user.get().getRole() != User.Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("Only admins can create protocols.");
        }

        Optional<ProtocolType> protocolType = protocolTypeRepository.findById(requestBody.getProtocolTypeId());
        if (protocolType.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid protocol type ID.");
        }

        Protocol protocol = new Protocol(requestBody.getName(), protocolType.get(), user.get());
        protocolRepository.save(protocol);

        return ResponseEntity.ok("Protocol created successfully.");
    }

    // DTO Class for JSON Request Body
    static class ProtocolRequest {
        private String name;
        private int protocolTypeId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getProtocolTypeId() { return protocolTypeId; }
        public void setProtocolTypeId(int protocolTypeId) { this.protocolTypeId = protocolTypeId; }
    }
}
