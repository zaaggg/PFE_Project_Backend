package com.PFE.DTT.repository;

import com.PFE.DTT.model.SpecificControlCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecificControlCriteriaRepository extends JpaRepository<SpecificControlCriteria, Integer> {
}
