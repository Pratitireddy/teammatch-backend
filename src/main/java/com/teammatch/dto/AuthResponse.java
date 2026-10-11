package com.teammatch.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private Long userId;

    public AuthResponse(String token, String email, String fullName, Long userId) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Long getUserId() { return userId; }
}
