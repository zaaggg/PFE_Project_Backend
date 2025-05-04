package com.PFE.DTT.controller;

import com.PFE.DTT.dto.PlantDTO;
import com.PFE.DTT.model.Plant;
import com.PFE.DTT.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin-plants")
@RequiredArgsConstructor
public class AdminPlantController {

    private final PlantRepository plantRepository;

    // ✅ Add new plant
    @PostMapping(path = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addPlant(@RequestBody PlantDTO dto) {
        if (plantRepository.existsByName(dto.getName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Plant with that name already exists."));
        }

        Plant plant = new Plant();
        plant.setName(dto.getName());         // ✅ Don't forget this
        plant.setAddress(dto.getAddress());   // ✅ THIS IS MISSING MOST OFTEN

        plantRepository.save(plant);

        return ResponseEntity.ok(Map.of("message", "Plant added successfully"));
    }


    // ✅ Delete plant by ID
    @DeleteMapping(path = "/delete/{plantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deletePlant(@PathVariable Long plantId) {
        if (!plantRepository.existsById(Math.toIntExact(plantId))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Plant not found"));
        }

        plantRepository.deleteById(Math.toIntExact(plantId));
        return ResponseEntity.ok(Map.of("message", "Plant deleted successfully"));
    }
}
