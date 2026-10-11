package com.teammatch.service;

import com.teammatch.dto.TeamDto;
import com.teammatch.dto.TeamMemberDto;
import com.teammatch.entity.Opportunity;
import com.teammatch.entity.OpportunityRole;
import com.teammatch.entity.Team;
import com.teammatch.entity.TeamMember;
import com.teammatch.entity.TeamMemberType;
import com.teammatch.entity.User;
import com.teammatch.exception.UnauthorizedException;
import com.teammatch.repository.TeamMemberRepository;
import com.teammatch.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final OpportunityService opportunityService;
    private final UserService userService;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, OpportunityService opportunityService, UserService userService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.opportunityService = opportunityService;
        this.userService = userService;
    }

    @Transactional
    public void addMemberToTeam(Opportunity opportunity, User student, OpportunityRole role) {
        Team team = teamRepository.findByOpportunityId(opportunity.getId())
                .orElseGet(() -> createTeam(opportunity));

        if (!teamMemberRepository.existsByTeamIdAndStudentId(team.getId(), student.getId())) {
            TeamMember member = new TeamMember();
            member.setTeam(team);
            member.setStudent(student);
            member.setRole(role);
            member.setMemberType(TeamMemberType.MEMBER);
            teamMemberRepository.save(member);
        }
    }

    private Team createTeam(Opportunity opportunity) {
        Team team = new Team();
        team.setOpportunity(opportunity);
        team = teamRepository.save(team);

        TeamMember organizer = new TeamMember();
        organizer.setTeam(team);
        organizer.setStudent(opportunity.getCreator());
        organizer.setRole(null);
        organizer.setMemberType(TeamMemberType.ORGANIZER);
        teamMemberRepository.save(organizer);

        return team;
    }

    public List<TeamDto> getMyTeams(Long studentId) {
        List<TeamMember> memberships = teamMemberRepository.findByStudentId(studentId);
        return memberships.stream()
                .map(m -> getTeamDetails(m.getTeam().getId(), studentId))
                .collect(Collectors.toList());
    }

    public TeamDto getTeamDetails(Long teamId, Long currentUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        boolean isMember = teamMemberRepository.existsByTeamIdAndStudentId(team.getId(), currentUserId);
        boolean isCreator = team.getOpportunity().getCreator().getId().equals(currentUserId);
        
        if (!isMember && !isCreator) {
            throw new UnauthorizedException("Not authorized to view this team");
        }

        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
        
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setOpportunity(opportunityService.mapToDto(team.getOpportunity()));
        dto.setMembers(members.stream().map(this::mapToMemberDto).collect(Collectors.toList()));
        dto.setCreatedAt(team.getCreatedAt());
        return dto;
    }

    private TeamMemberDto mapToMemberDto(TeamMember member) {
        TeamMemberDto dto = new TeamMemberDto();
        dto.setId(member.getId());
        dto.setStudent(userService.mapToDto(member.getStudent()));
        dto.setRole(member.getRole() != null ? opportunityService.mapRoleToDto(member.getRole()) : null);
        dto.setMemberType(member.getMemberType().name());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }
}
