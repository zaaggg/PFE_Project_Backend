package com.PFE.DTT.repository;

import com.PFE.DTT.model.ProtocolType;
import com.PFE.DTT.model.ReportValidation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportValidationRepository extends JpaRepository<ReportValidation, Long> {

    List<ReportValidation> findByProtocolType(ProtocolType protocolType); // ✅ Add this!
}
