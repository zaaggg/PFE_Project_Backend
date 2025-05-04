package com.PFE.DTT.repository;

import com.PFE.DTT.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    boolean existsByNameIgnoreCase(String name);
}