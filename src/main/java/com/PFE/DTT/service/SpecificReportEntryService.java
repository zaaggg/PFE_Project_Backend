package com.PFE.DTT.service;

import com.PFE.DTT.dto.SpecificReportEntryDTO;
import com.PFE.DTT.dto.SpecificReportEntryUpdateRequest;
import com.PFE.DTT.model.SpecificReportEntry;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.SpecificReportEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecificReportEntryService {

    private final SpecificReportEntryRepository repository;

    public List<SpecificReportEntryDTO> getEntriesForUser(Long reportId, User user) {
        return repository.findByReportId(reportId).stream()
                .filter(entry ->
                        entry.getReport().getAssignedUsers().contains(user) &&
                                entry.getSpecificControlCriteria().getCheckResponsibles().stream()
                                        .anyMatch(dep -> dep.getId() == (user.getDepartment().getId()))
                )
                .map(SpecificReportEntryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> updateEntry(int entryId, SpecificReportEntryUpdateRequest req, User user) {
        Optional<SpecificReportEntry> entryOpt = repository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Entry not found");
        }

        SpecificReportEntry entry = entryOpt.get();

        if (entry.isUpdated()) {
            return ResponseEntity.badRequest().body("This entry has already been updated.");
        }

        boolean isAssignedUser = entry.getReport().getAssignedUsers().contains(user);
        boolean isCheckDept = entry.getSpecificControlCriteria().getCheckResponsibles().stream()
                .anyMatch(dep -> dep.getId() == (user.getDepartment().getId()));

        if (!isAssignedUser || !isCheckDept) {
            return ResponseEntity.status(403).body("You are not authorized to update this entry");
        }

        if (req.getHomologation() == null) {
            return ResponseEntity.badRequest().body("Homologation field is required");
        }

        entry.setHomologation(req.getHomologation());
        if (req.getHomologation()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            if (req.getAction() == null || req.getResponsableAction() == null ||
                    req.getDeadline() == null || req.getSuccessControl() == null) {
                return ResponseEntity.badRequest().body("All fields are required when homologation is false");
            }
            entry.setAction(req.getAction());
            entry.setResponsableAction(req.getResponsableAction());
            entry.setDeadline(req.getDeadline());
            entry.setSuccessControl(req.getSuccessControl());
        }

        entry.setUpdated(true); // ✅ lock after update
        repository.save(entry);
        return ResponseEntity.ok("Specific entry updated successfully");
    }
}
