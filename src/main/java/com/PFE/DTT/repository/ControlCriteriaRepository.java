package com.PFE.DTT.repository;

import com.PFE.DTT.model.ControlCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlCriteriaRepository extends JpaRepository<ControlCriteria, Integer> {
}
