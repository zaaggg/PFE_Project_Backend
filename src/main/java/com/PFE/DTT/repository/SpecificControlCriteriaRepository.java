package com.PFE.DTT.repository;

import com.PFE.DTT.model.SpecificControlCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecificControlCriteriaRepository extends JpaRepository<SpecificControlCriteria, Integer> {
    List<SpecificControlCriteria> findByProtocolId(int protocolId); // Assure-toi d'avoir cette méthode

}
