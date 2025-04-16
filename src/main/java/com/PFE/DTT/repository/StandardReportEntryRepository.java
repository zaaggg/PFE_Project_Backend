package com.PFE.DTT.repository;

import com.PFE.DTT.model.StandardReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardReportEntryRepository extends JpaRepository<StandardReportEntry, Integer> {

    List<StandardReportEntry> findByReportId(Long reportId);
}