package com.PFE.DTT.repository;

import com.PFE.DTT.model.DepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentTypeRepository extends JpaRepository<DepartmentType, Integer> {
}