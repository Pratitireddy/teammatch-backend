package com.teammatch.controller;

import com.teammatch.dto.TeamDto;
import com.teammatch.entity.User;
import com.teammatch.repository.UserRepository;
import com.teammatch.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final UserRepository userRepository;

    public TeamController(TeamService teamService, UserRepository userRepository) {
        this.teamService = teamService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<TeamDto>> getMyTeams(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(teamService.getMyTeams(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeam(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(teamService.getTeamDetails(id, user.getId()));
    }
}
