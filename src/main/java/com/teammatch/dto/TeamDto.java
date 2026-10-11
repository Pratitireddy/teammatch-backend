package com.teammatch.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TeamDto {
    private Long id;
    private OpportunityDto opportunity;
    private List<TeamMemberDto> members;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OpportunityDto getOpportunity() { return opportunity; }
    public void setOpportunity(OpportunityDto opportunity) { this.opportunity = opportunity; }
    public List<TeamMemberDto> getMembers() { return members; }
    public void setMembers(List<TeamMemberDto> members) { this.members = members; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
