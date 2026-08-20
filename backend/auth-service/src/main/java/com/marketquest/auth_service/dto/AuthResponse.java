package com.marketquest.auth_service.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String mobile;
    private String filename;

    public AuthResponse(String token, String name, String email, String mobile, String filename) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.filename = filename;
    }

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public String getFilename() { return filename; }
}
