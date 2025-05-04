package com.PFE.DTT.controller;

import com.PFE.DTT.dto.PasswordUpdateRequest;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;
    @Autowired private UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        try {
            String message = authService.register(user);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyUser(@RequestParam String code) {
        try {
            String message = authService.verifyUser(code);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Received login request for email: {}", loginRequest.getEmail());

        try {
            // Try login first
            String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("Login successful for email: {}", loginRequest.getEmail());

            // After login success -> update loggedIn field
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setLoggedIn(true);
                userRepository.save(user); // Save updated user
                logger.info("User {} marked as logged in.", user.getEmail());
            } else {
                logger.warn("User {} not found after successful login.", loginRequest.getEmail());
            }

            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update-profile-photo")
    public ResponseEntity<String> updateProfilePhoto(@RequestHeader("Authorization") String token,
                                                     @RequestParam MultipartFile photo) {
        try {
            // Extract email from token using AuthService
            String email = authService.getEmailFromToken(token);

            // Update the profile photo using the email
            String response = authService.updateProfilePhoto(email, photo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }


    }

    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestHeader("Authorization") String token,
                                                              @RequestBody PasswordUpdateRequest request) {
        try {
            String message = authService.updatePassword(token, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // Inner class for login request
    static class LoginRequest {
        private String email;
        private String password;

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
