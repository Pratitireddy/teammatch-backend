package com.teammatch.dto;

import jakarta.validation.constraints.NotNull;

public class CreateApplicationRequest {
    @NotNull(message = "Opportunity ID is required")
    private Long opportunityId;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private String message;

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
