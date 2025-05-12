package com.PFE.DTT.controller;

import com.PFE.DTT.dto.UpdateProfileDTO;
import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.Plant;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.DepartmentRepository;
import com.PFE.DTT.repository.PlantRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PlantRepository plantRepository;


    /**
     * Update my profile (data + optional profile photo)
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal User user,
                                             @RequestPart("profileData") UpdateProfileDTO dto,
                                             @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {
        if (user == null) return ResponseEntity.status(401).body("User not authenticated");

        try {
            if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) user.setLastName(dto.getLastName());
            if (dto.getPhoneNumber() != null) user.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(Math.toIntExact(dto.getDepartmentId()))
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                user.setDepartment(department);
            }

            if (dto.getPlantId() != null) {
                Plant plant = plantRepository.findById(Math.toIntExact(dto.getPlantId()))
                        .orElseThrow(() -> new RuntimeException("Plant not found"));
                user.setPlant(plant);
            }


            if (dto.getEmail() != null) user.setEmail(dto.getEmail());


            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                String uploadedUrl = cloudinaryService.uploadProfilePhoto(profilePhoto);
                user.setProfilePhoto(uploadedUrl);
            }

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating profile: " + e.getMessage());
        }
    }
}
