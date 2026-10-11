package com.teammatch.controller;

import com.teammatch.dto.*;
import com.teammatch.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final com.teammatch.service.ApplicationService applicationService;

    public OpportunityController(OpportunityService opportunityService, com.teammatch.service.ApplicationService applicationService) {
        this.opportunityService = opportunityService;
        this.applicationService = applicationService;
    }

    // POST /api/opportunities — create
    @PostMapping
    public ResponseEntity<OpportunityDto> createOpportunity(
            Authentication authentication,
            @Valid @RequestBody CreateOpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.createOpportunity(authentication.getName(), request));
    }

    // GET /api/opportunities/{id} — get by id (authenticated)
    @GetMapping("/{id}")
    public ResponseEntity<OpportunityDto> getOpportunity(@PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.getOpportunityById(id));
    }

    // GET /api/opportunities?type=&keyword=&page=&size= — search open opportunities
    @GetMapping
    public ResponseEntity<Page<OpportunityDto>> listOpportunities(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(opportunityService.listOpenOpportunities(type, keyword, page, size));
    }

    // GET /api/opportunities/my — list creator's own opportunities
    @GetMapping("/my")
    public ResponseEntity<List<OpportunityDto>> getMyOpportunities(Authentication authentication) {
        return ResponseEntity.ok(opportunityService.getMyOpportunities(authentication.getName()));
    }

    // PUT /api/opportunities/{id} — update
    @PutMapping("/{id}")
    public ResponseEntity<OpportunityDto> updateOpportunity(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        return ResponseEntity.ok(opportunityService.updateOpportunity(authentication.getName(), id, request));
    }

    // PATCH /api/opportunities/{id}/close — close recruitment
    @PatchMapping("/{id}/close")
    public ResponseEntity<OpportunityDto> closeOpportunity(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.closeOpportunity(authentication.getName(), id));
    }

    // PATCH /api/opportunities/{id}/team-formed — mark team formed
    @PatchMapping("/{id}/team-formed")
    public ResponseEntity<OpportunityDto> markTeamFormed(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.markTeamFormed(authentication.getName(), id));
    }

    // PATCH /api/opportunities/{id}/complete — mark completed
    @PatchMapping("/{id}/complete")
    public ResponseEntity<OpportunityDto> markCompleted(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.markCompleted(authentication.getName(), id));
    }

    // PATCH /api/opportunities/{id}/archive — archive
    @PatchMapping("/{id}/archive")
    public ResponseEntity<OpportunityDto> archiveOpportunity(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.archiveOpportunity(authentication.getName(), id));
    }

    // GET /api/opportunities/{id}/applications — get applications for an opportunity
    @GetMapping("/{id}/applications")
    public ResponseEntity<List<com.teammatch.dto.ApplicationDto>> getApplicationsForOpportunity(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getOpportunityApplications(id, authentication.getName()));
    }
}
