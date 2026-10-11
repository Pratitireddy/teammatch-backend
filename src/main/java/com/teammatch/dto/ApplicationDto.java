package com.teammatch.dto;

import java.time.LocalDateTime;

public class ApplicationDto {
    private Long id;
    private UserProfileDto student;
    private OpportunityDto opportunity;
    private RoleDto role;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserProfileDto getStudent() { return student; }
    public void setStudent(UserProfileDto student) { this.student = student; }
    public OpportunityDto getOpportunity() { return opportunity; }
    public void setOpportunity(OpportunityDto opportunity) { this.opportunity = opportunity; }
    public RoleDto getRole() { return role; }
    public void setRole(RoleDto role) { this.role = role; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
