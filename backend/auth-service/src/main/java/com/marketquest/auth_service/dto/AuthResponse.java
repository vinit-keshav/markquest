package com.marketquest.auth_service.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String mobile;

    public AuthResponse(String token, String name, String email, String mobile) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
}
