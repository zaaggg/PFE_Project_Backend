// SpecificReportEntryService.java
package com.PFE.DTT.service;

import com.PFE.DTT.dto.SpecificChecklistItemDTO;
import com.PFE.DTT.dto.SpecificReportEntryDTO;
import com.PFE.DTT.model.Department;
import com.PFE.DTT.model.SpecificReportEntry;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.SpecificReportEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecificReportEntryService {

    private final SpecificReportEntryRepository repository;

    public List<SpecificChecklistItemDTO> getChecklistForUser(Long reportId, User user) {
        return repository.findByReportId(reportId).stream()
                .filter(entry ->
                        entry.getReport().getAssignedUsers().contains(user) &&
                                entry.getSpecificControlCriteria().getCheckResponsibles().stream()
                                        .anyMatch(dep -> dep.getId() == user.getDepartment().getId())
                )
                .map(entry -> {
                    SpecificChecklistItemDTO dto = new SpecificChecklistItemDTO();
                    dto.setCriteriaId(entry.getSpecificControlCriteria().getId());
                    dto.setCriteriaDescription(entry.getSpecificControlCriteria().getDescription());
                    dto.setCheckResponsibles((List<Department>) entry.getSpecificControlCriteria().getCheckResponsibles());
                    dto.setImplementationResponsibles((List<Department>) entry.getSpecificControlCriteria().getImplementationResponsibles());
                    dto.setHomologation(entry.getHomologation());
                    dto.setAction(entry.getAction());
                    dto.setResponsableAction(entry.getResponsableAction());
                    dto.setDeadline(entry.getDeadline());
                    dto.setSuccessControl(entry.getSuccessControl());
                    dto.setUpdated(entry.isUpdated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public String updateEntry(int entryId, SpecificReportEntryDTO req, User user) {
        SpecificReportEntry entry = repository.findById(entryId).orElse(null);
        if (entry == null) return "Entry not found";
        if (entry.isUpdated()) return "This entry is already updated";

        boolean isAssigned = entry.getReport().getAssignedUsers().contains(user);
        boolean isCorrectDept = entry.getSpecificControlCriteria().getCheckResponsibles().stream()
                .anyMatch(dep -> dep.getId() == user.getDepartment().getId());

        if (!isAssigned || !isCorrectDept) return "Unauthorized";

        entry.setHomologation(req.isHomologation());
        if (req.isHomologation()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            entry.setAction(req.getAction());
            entry.setResponsableAction(req.getResponsableAction());
            entry.setDeadline(req.getDeadline());
            entry.setSuccessControl(req.getSuccessControl());
        }

        entry.setUpdated(true);
        repository.save(entry);
        return "OK";
    }
}