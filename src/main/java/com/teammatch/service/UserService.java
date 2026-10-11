package com.teammatch.service;

import com.teammatch.dto.UpdateProfileRequest;
import com.teammatch.dto.UserProfileDto;
import com.teammatch.entity.Skill;
import com.teammatch.entity.User;
import com.teammatch.repository.SkillRepository;
import com.teammatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public UserService(UserRepository userRepository, SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getCollege() != null && !request.getCollege().isBlank()) {
            if (user.getCollege() != null && !user.getCollege().isBlank() && !user.getCollege().equals(request.getCollege())) {
                throw new RuntimeException("College cannot be changed after profile completion.");
            }
            user.setCollege(request.getCollege());
        }

        if (request.getCourse() != null) user.setCourse(request.getCourse());
        if (request.getYear() != null) user.setYear(request.getYear());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getPhotoUrl() != null) user.setPhotoUrl(request.getPhotoUrl());
        if (request.getGithubUrl() != null) user.setGithubUrl(request.getGithubUrl());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());

        if (request.getSkills() != null) {
            Set<Skill> newSkills = new HashSet<>();
            for (String skillName : request.getSkills()) {
                String normalizedName = skillName.trim().toUpperCase();
                Skill skill = skillRepository.findByName(normalizedName).orElseGet(() -> {
                    Skill s = new Skill();
                    s.setName(normalizedName);
                    return skillRepository.save(s);
                });
                newSkills.add(skill);
            }
            user.setSkills(newSkills);
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    public UserProfileDto mapToDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setCollege(user.getCollege());
        dto.setCourse(user.getCourse());
        dto.setYear(user.getYear());
        dto.setPhone(user.getPhone());
        dto.setBio(user.getBio());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setGithubUrl(user.getGithubUrl());
        dto.setLinkedinUrl(user.getLinkedinUrl());
        
        List<String> skillNames = user.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList());
        dto.setSkills(skillNames);
        
        return dto;
    }
}
