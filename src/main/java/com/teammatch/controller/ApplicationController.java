package com.teammatch.controller;

import com.teammatch.dto.ApplicationDto;
import com.teammatch.dto.CreateApplicationRequest;
import com.teammatch.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> apply(@Valid @RequestBody CreateApplicationRequest request, Authentication authentication) {
        return ResponseEntity.ok(applicationService.applyToOpportunity(authentication.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationDto>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplication(id, authentication.getName()));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ApplicationDto> acceptApplication(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(applicationService.acceptApplication(id, authentication.getName()));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApplicationDto> rejectApplication(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(applicationService.rejectApplication(id, authentication.getName()));
    }
}
