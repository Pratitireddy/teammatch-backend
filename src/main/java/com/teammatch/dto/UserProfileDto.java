package com.teammatch.dto;

import java.util.List;

public class UserProfileDto {
    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String course;
    private String year;
    private String phone;
    private String bio;
    private String photoUrl;
    private String githubUrl;
    private String linkedinUrl;
    private List<String> skills;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
