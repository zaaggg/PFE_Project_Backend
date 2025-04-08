package com.PFE.DTT.controller;

import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.Plant;
import com.PFE.DTT.repository.DepartmentRepository;
import com.PFE.DTT.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PlantRepository plantRepository;

    // ✅ Get all departments (no auth required)
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return ResponseEntity.ok(departments);
    }

    // ✅ Get all plants (no auth required)
    @GetMapping("/plants")
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();
        return ResponseEntity.ok(plants);
    }
}
