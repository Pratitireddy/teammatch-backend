package com.teammatch.dto;

import com.teammatch.entity.OpportunityLinkType;
import com.teammatch.entity.OpportunityMode;
import com.teammatch.entity.OpportunityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OpportunityDto {
    private Long id;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String description;
    private String type;
    private OpportunityStatus status;
    private OpportunityMode mode;
    private String location;
    private OpportunityLinkType linkType;
    private String externalLink;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime applicationDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RoleDto> roles;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<RoleDto> getRoles() { return roles; }
    public void setRoles(List<RoleDto> roles) { this.roles = roles; }
}
