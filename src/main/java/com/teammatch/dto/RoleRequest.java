package com.teammatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class RoleRequest {

    @NotBlank(message = "Role title is required")
    private String title;

    @Positive(message = "Vacancies must be at least 1")
    private int vacancies = 1;

    private List<String> requiredSkills;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getVacancies() { return vacancies; }
    public void setVacancies(int vacancies) { this.vacancies = vacancies; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
}
