package com.PFE.DTT.controller;

import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/promote-to-admin")
    public ResponseEntity<String> promoteToAdmin(
            @RequestHeader("Authorization") String token,
            @RequestParam String userEmail) {

        String requesterEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        logger.info("Requester email: {}", requesterEmail);

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> {
                    logger.error("Requester not found in DB");
                    return new RuntimeException("Requester not found");
                });

        if (!requester.getRole().equals(User.Role.ROLE_ADMIN)) {
            logger.warn("Unauthorized access attempt by: {}", requesterEmail);
            return ResponseEntity.status(403).body("Unauthorized: Only admins can promote users");
        }

        User userToPromote = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userToPromote.getRole().equals(User.Role.ROLE_ADMIN)) {
            return ResponseEntity.badRequest().body("User is already an admin");
        }

        userToPromote.setRole(User.Role.ROLE_ADMIN);
        userRepository.save(userToPromote);

        logger.info("User {} promoted to admin successfully", userEmail);
        return ResponseEntity.ok("User promoted to admin successfully. They must log out and log back in to update their role.");
    }
}
