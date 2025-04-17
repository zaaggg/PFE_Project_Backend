package com.PFE.DTT.service;

import com.PFE.DTT.dto.StandardChecklistItemDTO;
import com.PFE.DTT.dto.StandardReportEntryDTO;
import com.PFE.DTT.model.StandardReportEntry;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.StandardReportEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandardReportEntryService {

    private final StandardReportEntryRepository repository;

    public List<StandardChecklistItemDTO> getEntriesForUser(Long reportId, User user) {
        return repository.findByReportId(reportId).stream()
                .filter(entry ->
                        entry.getReport().getAssignedUsers().contains(user) &&
                                entry.getStandardControlCriteria().getCheckResponsible().getId() == (user.getDepartment().getId())
                )
                .map(entry -> {
                    StandardChecklistItemDTO dto = new StandardChecklistItemDTO();
                    dto.setCriteriaId(entry.getStandardControlCriteria().getId());
                    dto.setCriteriaDescription(entry.getStandardControlCriteria().getDescription());
                    dto.setCheckResponsible(entry.getStandardControlCriteria().getCheckResponsible());
                    dto.setImplementationResponsible(entry.getStandardControlCriteria().getImplementationResponsible());
                    dto.setImplemented(entry.isImplemented());
                    dto.setAction(entry.getAction());
                    dto.setResponsableAction(entry.getResponsableAction());
                    dto.setDeadline(entry.getDeadline());
                    dto.setSuccessControl(entry.getSuccessControl());
                    dto.setUpdated(entry.getIsUpdated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<StandardChecklistItemDTO> getChecklistForUser(Long reportId, User user) {
        return repository.findByReportId(reportId).stream()
                .filter(entry -> entry.getReport().getAssignedUsers().contains(user)
                        && entry.getStandardControlCriteria().getCheckResponsible().getId() == (user.getDepartment().getId()))
                .map(entry -> {
                    StandardChecklistItemDTO dto = new StandardChecklistItemDTO();
                    dto.setCriteriaId(entry.getStandardControlCriteria().getId());
                    dto.setCriteriaDescription(entry.getStandardControlCriteria().getDescription());
                    dto.setCheckResponsible(entry.getStandardControlCriteria().getCheckResponsible());
                    dto.setImplementationResponsible(entry.getStandardControlCriteria().getImplementationResponsible());
                    dto.setImplemented(entry.isImplemented());
                    dto.setAction(entry.getAction());
                    dto.setResponsableAction(entry.getResponsableAction());
                    dto.setDeadline(entry.getDeadline());
                    dto.setSuccessControl(entry.getSuccessControl());
                    dto.setUpdated(entry.getIsUpdated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public String updateEntry(int entryId, StandardReportEntryDTO dto, User user) {
        Optional<StandardReportEntry> opt = repository.findById(entryId);
        if (opt.isEmpty()) return "Entry not found";

        StandardReportEntry entry = opt.get();

        if (entry.getIsUpdated()) return "Entry already updated";

        boolean isAssigned = entry.getReport().getAssignedUsers().contains(user);
        boolean isCorrectDept = entry.getStandardControlCriteria()
                .getCheckResponsible().getId() == (user.getDepartment().getId());

        if (!isAssigned || !isCorrectDept) return "Unauthorized";

        entry.setImplemented(dto.isImplemented());

        if (dto.isImplemented()) {
            entry.setAction(null);
            entry.setResponsableAction(null);
            entry.setDeadline(null);
            entry.setSuccessControl(null);
        } else {
            entry.setAction(dto.getAction());
            entry.setResponsableAction(dto.getResponsableAction());
            entry.setDeadline(dto.getDeadline());
            entry.setSuccessControl(dto.getSuccessControl());
        }

        entry.setUpdated(true);
        repository.save(entry);

        return "OK";
    }


}
