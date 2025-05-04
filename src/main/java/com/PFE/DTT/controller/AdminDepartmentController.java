package com.PFE.DTT.controller;

import com.PFE.DTT.model.Department;
import com.PFE.DTT.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-departments")
@RequiredArgsConstructor
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addDepartment(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Department name is required"));
        }

        boolean exists = departmentRepository.existsByNameIgnoreCase(name.trim());
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Department already exists"));
        }

        Department department = new Department();
        department.setName(name.trim());
        departmentRepository.save(department);

        return ResponseEntity.ok(Map.of("message", "Department added successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        if (!departmentRepository.existsById(Math.toIntExact(id))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Department not found"));
        }

        departmentRepository.deleteById(Math.toIntExact(id));
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }
}
