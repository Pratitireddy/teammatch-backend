package com.teammatch.dto;

import com.teammatch.entity.OpportunityLinkType;
import com.teammatch.entity.OpportunityMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CreateOpportunityRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String type;

    private OpportunityMode mode;

    @NotBlank(message = "Location is required")
    private String location;

    private OpportunityLinkType linkType;
    private String externalLink;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDateTime applicationDeadline;

    @NotNull(message = "At least one role is required")
    @Size(min = 1, message = "At least one role is required")
    @Valid
    private List<RoleRequest> roles;

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public OpportunityMode getMode() { return mode; }
    public void setMode(OpportunityMode mode) { this.mode = mode; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public OpportunityLinkType getLinkType() { return linkType; }
    public void setLinkType(OpportunityLinkType linkType) { this.linkType = linkType; }
    public String getExternalLink() { return externalLink; }
    public void setExternalLink(String externalLink) { this.externalLink = externalLink; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDateTime getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDateTime applicationDeadline) { this.applicationDeadline = applicationDeadline; }
    public List<RoleRequest> getRoles() { return roles; }
    public void setRoles(List<RoleRequest> roles) { this.roles = roles; }
}
