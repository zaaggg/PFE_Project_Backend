package com.PFE.DTT.repository;

import com.PFE.DTT.model.SpecificReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecificReportEntryRepository extends JpaRepository<SpecificReportEntry, Integer> {

    List<SpecificReportEntry> findByReportId(Long reportId);

}