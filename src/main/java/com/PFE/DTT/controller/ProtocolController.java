package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ProtocolCreationRequest;
import com.PFE.DTT.dto.ProtocolDTO;
import com.PFE.DTT.dto.SpecificCriteriaDTO;
import com.PFE.DTT.model.*;
import com.PFE.DTT.repository.ProtocolRepository;
import com.PFE.DTT.repository.SpecificControlCriteriaRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil; // ✅ Utility class to extract user ID from token
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired private SpecificControlCriteriaRepository specificControlCriteriaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // ✅ Inject JWT Utility

    @GetMapping("/grouped")
    public ResponseEntity<?> getProtocolsGroupedByType(@AuthenticationPrincipal User user) {
        if (user.getRole() != User.Role.DEPARTMENT_MANAGER && user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("Only department managers or admin can create reports.");
        }

        List<ProtocolDTO> protocolDTOs = protocolRepository.findAll().stream()
                .map(ProtocolDTO::new)
                .collect(Collectors.toList());

        Map<ProtocolType, List<ProtocolDTO>> grouped = protocolDTOs.stream()
                .collect(Collectors.groupingBy(ProtocolDTO::getProtocolType));

        return ResponseEntity.ok(grouped);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProtocol(@RequestBody ProtocolCreationRequest request,
                                            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can create protocols.");
        }

        Protocol protocol = new Protocol();
        protocol.setName(request.getName());
        protocol.setProtocolType(request.getProtocolType());
        protocol.setCreatedBy(currentUser); // ✅ THIS LINE IS REQUIRED

        Protocol savedProtocol = protocolRepository.save(protocol);

        for (SpecificCriteriaDTO dto : request.getSpecificCriteria()) {
            SpecificControlCriteria criteria = new SpecificControlCriteria();
            criteria.setDescription(dto.getDescription());
            criteria.setProtocol(protocol);

            criteria.setImplementationResponsibles(new HashSet<>(dto.getImplementationResponsibles()));
            criteria.setCheckResponsibles(new HashSet<>(dto.getCheckResponsibles()));

            specificControlCriteriaRepository.save(criteria);
        }


        Map<String, String> response = new HashMap<>();
        response.put("message", "Protocol created successfully.");
        return ResponseEntity.ok(response);

    }




}
