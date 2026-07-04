package com.marketquest.auth_service.dto;

public class SignupRequest {
    private String nameee;
    private String email;
    private String mobile;
    private String password;

    public String getName() { return nameee; }
    public void setName(String nameee) { this.nameee = nameee; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}