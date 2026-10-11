package com.teammatch.dto;

import java.time.LocalDateTime;

public class TeamMemberDto {
    private Long id;
    private UserProfileDto student;
    private RoleDto role;
    private String memberType;
    private LocalDateTime joinedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserProfileDto getStudent() { return student; }
    public void setStudent(UserProfileDto student) { this.student = student; }
    public RoleDto getRole() { return role; }
    public void setRole(RoleDto role) { this.role = role; }
    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
