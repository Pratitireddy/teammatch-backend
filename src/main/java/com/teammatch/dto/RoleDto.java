package com.teammatch.dto;

import java.util.List;

public class RoleDto {
    private Long id;
    private String title;
    private int vacancies;
    private int filledCount;
    private List<String> requiredSkills;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getVacancies() { return vacancies; }
    public void setVacancies(int vacancies) { this.vacancies = vacancies; }
    public int getFilledCount() { return filledCount; }
    public void setFilledCount(int filledCount) { this.filledCount = filledCount; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
}
