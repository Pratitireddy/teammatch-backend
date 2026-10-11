package com.teammatch.service;

import com.teammatch.dto.ApplicationDto;
import com.teammatch.dto.CreateApplicationRequest;
import com.teammatch.entity.*;
import com.teammatch.exception.UnauthorizedException;
import com.teammatch.repository.ApplicationRepository;
import com.teammatch.repository.OpportunityRepository;
import com.teammatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final TeamService teamService;
    private final NotificationService notificationService;
    private final OpportunityService opportunityService;
    private final UserService userService;

    public ApplicationService(ApplicationRepository applicationRepository, OpportunityRepository opportunityRepository, UserRepository userRepository, TeamService teamService, NotificationService notificationService, OpportunityService opportunityService, UserService userService) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.teamService = teamService;
        this.notificationService = notificationService;
        this.opportunityService = opportunityService;
        this.userService = userService;
    }

    @Transactional
    public ApplicationDto applyToOpportunity(String email, CreateApplicationRequest request) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Opportunity opportunity = opportunityRepository.findById(request.getOpportunityId())
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new RuntimeException("Opportunity is not open for applications");
        }

        if (opportunity.getApplicationDeadline() != null && opportunity.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Application deadline has passed");
        }

        OpportunityRole role = opportunity.getRoles().stream()
                .filter(r -> r.getId().equals(request.getRoleId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role does not belong to the specified opportunity"));

        if (role.getFilledCount() >= role.getVacancies()) {
            throw new RuntimeException("Role has no available capacity");
        }

        if (applicationRepository.existsByStudentIdAndOpportunityIdAndRoleId(student.getId(), opportunity.getId(), role.getId())) {
            throw new RuntimeException("Duplicate application for the same role");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setOpportunity(opportunity);
        application.setRole(role);
        application.setMessage(request.getMessage());
        application.setStatus(ApplicationStatus.PENDING);

        application = applicationRepository.save(application);

        notificationService.createNotification(
                opportunity.getCreator(),
                NotificationType.NEW_APPLICATION,
                "New Application Received",
                student.getFullName() + " applied for " + role.getTitle() + " in " + opportunity.getTitle()
        );

        return mapToDto(application);
    }

    public List<ApplicationDto> getMyApplications(String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return applicationRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ApplicationDto> getOpportunityApplications(Long opportunityId, String email) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));
        
        if (!opportunity.getCreator().getEmail().equals(email)) {
            throw new UnauthorizedException("Not authorized to view applications for this opportunity");
        }

        return applicationRepository.findByOpportunityId(opportunityId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ApplicationDto getApplication(Long id, String email) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        boolean isApplicant = application.getStudent().getEmail().equals(email);
        boolean isCreator = application.getOpportunity().getCreator().getEmail().equals(email);

        if (!isApplicant && !isCreator) {
            throw new UnauthorizedException("Not authorized to view this application");
        }

        return mapToDto(application);
    }

    @Transactional
    public ApplicationDto acceptApplication(Long id, String email) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Only PENDING applications can be accepted");
        }

        Opportunity opportunity = opportunityRepository.findByIdWithPessimisticLock(application.getOpportunity().getId())
                .orElseThrow(() -> new RuntimeException("Opportunity not found"));
        
        OpportunityRole role = opportunity.getRoles().stream()
                .filter(r -> r.getId().equals(application.getRole().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!opportunity.getCreator().getEmail().equals(email)) {
            throw new UnauthorizedException("Not authorized to accept applications for this opportunity");
        }

        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new RuntimeException("Opportunity is no longer open for accepting applicants");
        }

        if (role.getFilledCount() >= role.getVacancies()) {
            throw new RuntimeException("Selected role has no available capacity");
        }

        role.setFilledCount(role.getFilledCount() + 1);
        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);

        teamService.addMemberToTeam(opportunity, application.getStudent(), role);

        List<Application> otherPending = applicationRepository.findByStudentIdAndOpportunityIdAndStatus(
                application.getStudent().getId(),
                opportunity.getId(),
                ApplicationStatus.PENDING
        );
        
        for (Application otherApp : otherPending) {
            if (!otherApp.getId().equals(application.getId())) {
                otherApp.setStatus(ApplicationStatus.CANCELLED);
                applicationRepository.save(otherApp);
                notificationService.createNotification(
                        otherApp.getStudent(),
                        NotificationType.APPLICATION_CANCELLED,
                        "Application Cancelled",
                        "Your application for " + otherApp.getRole().getTitle() + " in " + opportunity.getTitle() + " was cancelled because you were accepted for another role."
                );
            }
        }

        notificationService.createNotification(
                application.getStudent(),
                NotificationType.APPLICATION_ACCEPTED,
                "Application Accepted",
                "Your application for " + role.getTitle() + " in " + opportunity.getTitle() + " was accepted!"
        );
        
        notificationService.createNotification(
                application.getStudent(),
                NotificationType.ADDED_TO_TEAM,
                "Added to Team",
                "You have been added to the team for " + opportunity.getTitle()
        );

        return mapToDto(application);
    }

    @Transactional
    public ApplicationDto rejectApplication(Long id, String email) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Opportunity opportunity = application.getOpportunity();

        if (!opportunity.getCreator().getEmail().equals(email)) {
            throw new UnauthorizedException("Not authorized to reject applications for this opportunity");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Only PENDING applications can be rejected");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        notificationService.createNotification(
                application.getStudent(),
                NotificationType.APPLICATION_REJECTED,
                "Application Rejected",
                "Your application for " + application.getRole().getTitle() + " in " + opportunity.getTitle() + " was rejected."
        );

        return mapToDto(application);
    }

    private ApplicationDto mapToDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setStudent(userService.mapToDto(application.getStudent()));
        dto.setOpportunity(opportunityService.mapToDto(application.getOpportunity()));
        dto.setRole(opportunityService.mapRoleToDto(application.getRole()));
        dto.setMessage(application.getMessage());
        dto.setStatus(application.getStatus().name());
        dto.setCreatedAt(application.getCreatedAt());
        return dto;
    }
}
