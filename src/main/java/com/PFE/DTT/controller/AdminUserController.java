package com.PFE.DTT.controller;

import com.PFE.DTT.dto.NewUserRequestDTO;
import com.PFE.DTT.dto.UpdateUserDTO;
import com.PFE.DTT.dto.UserDTO;
import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.Plant;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.DepartmentRepository;
import com.PFE.DTT.repository.PlantRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin-users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PlantRepository plantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public AdminUserController(UserRepository userRepository,
                               DepartmentRepository departmentRepository,
                               PlantRepository plantRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.plantRepository = plantRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }



    // ✅ Update user details (department, plant, role, phone)
    @PutMapping(value = "/update/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Department department = departmentRepository.findById(Math.toIntExact(dto.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Invalid department ID"));

        Plant plant = plantRepository.findById(Math.toIntExact(dto.getPlantId()))
                .orElseThrow(() -> new RuntimeException("Invalid plant ID"));

        user.setDepartment(department);
        user.setPlant(plant);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole());

        userRepository.save(user);

        // ✅ Ensures JSON response like { "message": "User updated successfully" }
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    // ✅ Add new user (isVerified = true, no verification code, send email)
    @PostMapping(path = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addUser(@RequestBody NewUserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists."));
        }

        Department department = departmentRepository.findById(Math.toIntExact(dto.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Invalid department ID"));

        Plant plant = plantRepository.findById(Math.toIntExact(dto.getPlantId()))
                .orElseThrow(() -> new RuntimeException("Invalid plant ID"));

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setFirstName(dto.getFirstName());
        newUser.setLastName(dto.getLastName());
        newUser.setPhoneNumber(dto.getPhoneNumber());
        newUser.setPassword(encodedPassword);
        newUser.setDepartment(department);
        newUser.setPlant(plant);
        newUser.setRole(dto.getRole());
        newUser.setVerified(true);
        newUser.setVerificationCode(null);

        userRepository.save(newUser);

        // Send welcome email
        String subject = "Your account has been created";
        String body = String.format("""
            The admin has created an account for you. Click the link below to login:

            http://localhost:4200/login

            Use this email: %s
            Use this password: %s
            """, newUser.getEmail(), rawPassword);

        emailService.sendEmail(newUser.getEmail(), subject, body);

        return ResponseEntity.ok(Map.of("message", "User added and email sent"));
    }


    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ✅ Delete a user by ID
    @DeleteMapping(value = "/delete/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }




}
