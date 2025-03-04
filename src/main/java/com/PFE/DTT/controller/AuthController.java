package com.PFE.DTT.controller;

import com.PFE.DTT.model.User;
import com.PFE.DTT.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        logger.info("Received registration request for email: {}", user.getEmail());

        try {
            String response = authService.register(user);
            logger.info("Registration successful for email: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Registration failed for email: {}", user.getEmail(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String code) {
        logger.info("Received verification request for code: {}", code);

        try {
            String response = authService.verifyUser(code);
            logger.info("Verification successful for code: {}", code);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Verification failed for code: {}", code, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Received login request for email: {}", loginRequest.getEmail());

        try {
            String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
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
