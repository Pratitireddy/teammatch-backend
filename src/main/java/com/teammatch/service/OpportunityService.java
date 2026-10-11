package com.teammatch.service;

import com.teammatch.dto.*;
import com.teammatch.entity.*;
import com.teammatch.exception.UnauthorizedException;
import com.teammatch.repository.OpportunityRepository;
import com.teammatch.repository.SkillRepository;
import com.teammatch.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final com.teammatch.repository.ApplicationRepository applicationRepository;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              UserRepository userRepository,
                              SkillRepository skillRepository,
                              com.teammatch.repository.ApplicationRepository applicationRepository) {
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.applicationRepository = applicationRepository;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto createOpportunity(String creatorEmail, CreateOpportunityRequest request) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateDates(request.getStartDate(), request.getEndDate(), request.getApplicationDeadline() != null
                ? request.getApplicationDeadline().toLocalDate() : null, true);

        Opportunity opportunity = new Opportunity();
        opportunity.setCreator(creator);
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setType(request.getType());
        opportunity.setMode(request.getMode());
        opportunity.setLocation(request.getLocation());
        opportunity.setLinkType(request.getLinkType());
        opportunity.setExternalLink(request.getExternalLink());
        opportunity.setStartDate(request.getStartDate());
        opportunity.setEndDate(request.getEndDate());
        opportunity.setApplicationDeadline(request.getApplicationDeadline());
        opportunity.setStatus(OpportunityStatus.OPEN);

        List<OpportunityRole> roles = buildRoles(request.getRoles(), opportunity);
        opportunity.setRoles(roles);

        Opportunity saved = opportunityRepository.save(opportunity);
        return mapToDto(saved);
    }

    // ----------------------------------------------------------------
    // GET BY ID (public)
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public OpportunityDto getOpportunityById(Long id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
        return mapToDto(opportunity);
    }

    // ----------------------------------------------------------------
    // LIST / SEARCH OPEN (public)
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<OpportunityDto> listOpenOpportunities(String type, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String normalizedType = (type != null && !type.isBlank()) ? type : null;
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        return opportunityRepository
                .findOpenOpportunities(OpportunityStatus.OPEN, normalizedType, normalizedKeyword, pageable)
                .map(this::mapToDto);
    }

    // ----------------------------------------------------------------
    // MY OPPORTUNITIES
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<OpportunityDto> getMyOpportunities(String email) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return opportunityRepository.findByCreatorId(creator.getId())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto updateOpportunity(String email, Long opportunityId, UpdateOpportunityRequest request) {
        Opportunity opportunity = findAndAuthorize(email, opportunityId);

        if (opportunity.getStatus() == OpportunityStatus.COMPLETED
                || opportunity.getStatus() == OpportunityStatus.ARCHIVED) {
            throw new RuntimeException("Cannot update a COMPLETED or ARCHIVED opportunity.");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            opportunity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            opportunity.setDescription(request.getDescription());
        }
        if (request.getType() != null) opportunity.setType(request.getType());
        if (request.getMode() != null) opportunity.setMode(request.getMode());
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            opportunity.setLocation(request.getLocation());
        }
        if (request.getLinkType() != null) opportunity.setLinkType(request.getLinkType());
        if (request.getExternalLink() != null) opportunity.setExternalLink(request.getExternalLink());
        if (request.getStartDate() != null) opportunity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) opportunity.setEndDate(request.getEndDate());
        if (request.getApplicationDeadline() != null) {
            opportunity.setApplicationDeadline(request.getApplicationDeadline());
        }

        validateDates(opportunity.getStartDate(), opportunity.getEndDate(),
                opportunity.getApplicationDeadline() != null
                        ? opportunity.getApplicationDeadline().toLocalDate() : null, false);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<com.teammatch.entity.Application> apps = applicationRepository.findByOpportunityId(opportunityId);
            if (!apps.isEmpty()) {
                throw new RuntimeException("Cannot modify opportunity roles after applications have been submitted.");
            }
            // Replace all roles (orphanRemoval on entity handles DB cleanup)
            opportunity.getRoles().clear();
            List<OpportunityRole> newRoles = buildRoles(request.getRoles(), opportunity);
            opportunity.getRoles().addAll(newRoles);
        }

        return mapToDto(opportunityRepository.save(opportunity));
    }

    // ----------------------------------------------------------------
    // CLOSE RECRUITMENT
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto closeOpportunity(String email, Long opportunityId) {
        Opportunity opportunity = findAndAuthorize(email, opportunityId);
        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new RuntimeException("Only an OPEN opportunity can be closed.");
        }
        opportunity.setStatus(OpportunityStatus.CLOSED);
        return mapToDto(opportunityRepository.save(opportunity));
    }

    // ----------------------------------------------------------------
    // MARK TEAM FORMED
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto markTeamFormed(String email, Long opportunityId) {
        Opportunity opportunity = findAndAuthorize(email, opportunityId);
        if (opportunity.getStatus() != OpportunityStatus.CLOSED) {
            throw new RuntimeException("Opportunity must be CLOSED before marking TEAM_FORMED.");
        }
        opportunity.setStatus(OpportunityStatus.TEAM_FORMED);
        return mapToDto(opportunityRepository.save(opportunity));
    }

    // ----------------------------------------------------------------
    // MARK COMPLETED
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto markCompleted(String email, Long opportunityId) {
        Opportunity opportunity = findAndAuthorize(email, opportunityId);
        if (opportunity.getStatus() != OpportunityStatus.TEAM_FORMED) {
            throw new RuntimeException("Opportunity must be in TEAM_FORMED status before marking COMPLETED.");
        }
        opportunity.setStatus(OpportunityStatus.COMPLETED);
        return mapToDto(opportunityRepository.save(opportunity));
    }

    // ----------------------------------------------------------------
    // ARCHIVE
    // ----------------------------------------------------------------
    @Transactional
    public OpportunityDto archiveOpportunity(String email, Long opportunityId) {
        Opportunity opportunity = findAndAuthorize(email, opportunityId);
        if (opportunity.getStatus() == OpportunityStatus.OPEN) {
            throw new RuntimeException("Close or complete the opportunity before archiving.");
        }
        if (opportunity.getStatus() == OpportunityStatus.ARCHIVED) {
            throw new RuntimeException("Opportunity is already archived.");
        }
        opportunity.setStatus(OpportunityStatus.ARCHIVED);
        return mapToDto(opportunityRepository.save(opportunity));
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------
    private Opportunity findAndAuthorize(String email, Long opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + opportunityId));
        if (!opportunity.getCreator().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to manage this opportunity.");
        }
        return opportunity;
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate,
                               java.time.LocalDate applicationDeadlineDate, boolean isNew) {
        if (isNew) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (startDate != null && startDate.isBefore(today)) {
                throw new RuntimeException("Start date cannot be in the past.");
            }
            if (applicationDeadlineDate != null && applicationDeadlineDate.isBefore(today)) {
                throw new RuntimeException("Application deadline cannot be in the past.");
            }
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date.");
        }
        if (applicationDeadlineDate != null && endDate != null
                && applicationDeadlineDate.isAfter(endDate)) {
            throw new RuntimeException("Application deadline cannot be after the end date.");
        }
    }

    private List<OpportunityRole> buildRoles(List<RoleRequest> roleRequests, Opportunity opportunity) {
        List<OpportunityRole> roles = new ArrayList<>();
        for (RoleRequest rr : roleRequests) {
            OpportunityRole role = new OpportunityRole();
            role.setOpportunity(opportunity);
            role.setTitle(rr.getTitle());
            role.setVacancies(rr.getVacancies());
            role.setFilledCount(0);

            if (rr.getRequiredSkills() != null) {
                Set<Skill> skills = new HashSet<>();
                for (String skillName : rr.getRequiredSkills()) {
                    String normalized = skillName.trim().toUpperCase();
                    Skill skill = skillRepository.findByName(normalized).orElseGet(() -> {
                        Skill s = new Skill();
                        s.setName(normalized);
                        return skillRepository.save(s);
                    });
                    skills.add(skill);
                }
                role.setRequiredSkills(skills);
            }
            roles.add(role);
        }
        return roles;
    }

    public RoleDto mapRoleToDto(OpportunityRole role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setTitle(role.getTitle());
        dto.setVacancies(role.getVacancies());
        dto.setFilledCount(role.getFilledCount());
        dto.setRequiredSkills(role.getRequiredSkills().stream()
                .map(Skill::getName).collect(Collectors.toList()));
        return dto;
    }

    public OpportunityDto mapToDto(Opportunity o) {
        OpportunityDto dto = new OpportunityDto();
        dto.setId(o.getId());
        dto.setCreatorId(o.getCreator().getId());
        dto.setCreatorName(o.getCreator().getFullName());
        dto.setTitle(o.getTitle());
        dto.setDescription(o.getDescription());
        dto.setType(o.getType());
        dto.setStatus(o.getStatus());
        dto.setMode(o.getMode());
        dto.setLocation(o.getLocation());
        dto.setLinkType(o.getLinkType());
        dto.setExternalLink(o.getExternalLink());
        dto.setStartDate(o.getStartDate());
        dto.setEndDate(o.getEndDate());
        dto.setApplicationDeadline(o.getApplicationDeadline());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());
        dto.setRoles(o.getRoles().stream().map(this::mapRoleToDto).collect(Collectors.toList()));
        return dto;
    }
}
