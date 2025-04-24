package com.PFE.DTT.repository;

import com.PFE.DTT.model.ReportValidation;
import com.PFE.DTT.model.ValidationEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationEntryRepository extends JpaRepository<ValidationEntry, Long> {

    List<ValidationEntry> findByReportId(Long reportId);

    List<ValidationEntry> findByReportValidationId(Long reportValidationId);
}
