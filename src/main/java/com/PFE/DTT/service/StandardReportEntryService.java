package com.PFE.DTT.service;

import com.PFE.DTT.dto.StandardReportEntryDTO;
import com.PFE.DTT.dto.StandardReportEntryUpdateRequest;
import com.PFE.DTT.model.StandardControlCriteria;
import com.PFE.DTT.model.StandardReportEntry;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.StandardReportEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StandardReportEntryService {

    private final StandardReportEntryRepository repository;

    @Autowired
    public StandardReportEntryService(StandardReportEntryRepository repository) {
        this.repository = repository;
    }

    public List<StandardReportEntryDTO> findByReportId(int reportId) {
        List<StandardReportEntry> entries = repository.findByReportId((long) reportId);
        return entries.stream()
                .map(StandardReportEntryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> updateEntry(int entryId, StandardReportEntryUpdateRequest req, User user) {
        Optional<StandardReportEntry> entryOpt = repository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Entry not found");
        }

        StandardReportEntry entry = entryOpt.get();

        if (entry.isUpdated()) {
            return ResponseEntity.badRequest().body("This entry has already been updated.");
        }

        StandardControlCriteria criteria = entry.getStandardControlCriteria();

        boolean isAssignedUser = entry.getReport().getAssignedUsers().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        boolean isCheckDept = criteria.getCheckResponsible().getId() == (user.getDepartment().getId());

        if (!isAssignedUser || !isCheckDept) {
            return ResponseEntity.status(403).body("You are not authorized to update this entry");
        }

        if (req.getImplemented() == null) {
            return ResponseEntity.badRequest().body("Implemented field is required");
        }

        entry.setImplemented(req.getImplemented());
        if (req.getImplemented()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            if (req.getAction() == null || req.getResponsableAction() == null ||
                    req.getDeadline() == null || req.getSuccessControl() == null) {
                return ResponseEntity.badRequest().body("All fields are required when implemented is false");
            }
            entry.setAction(req.getAction());
            entry.setResponsableAction(req.getResponsableAction());
            entry.setDeadline(req.getDeadline());
            entry.setSuccessControl(req.getSuccessControl());
        }

        entry.setUpdated(true); // lock it after update
        repository.save(entry);

        return ResponseEntity.ok("Standard entry updated successfully");
    }
}
