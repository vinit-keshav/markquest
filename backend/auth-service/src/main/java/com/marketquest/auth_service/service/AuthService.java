package com.marketquest.auth_service.service;

import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.entity.User;
import com.marketquest.auth_service.repository.UserRepository;
import com.marketquest.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
@Service

public class AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
  
  public String signup(SignupRequest request) {
     if(userRepository.existsByEmail(request.getEmail())) {
        return "Email already in use";
     }

     User user = new User();
     user.setName(request.getName());
     user.setEmail(request.getEmail());
     user.setPassword(passwordEncoder.encode(request.getPassword()));
     userRepository.save(user);
     return "User created successfully";
  }

  public List<AuthResponse> login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
    .orElseThrow(() -> new RuntimeException("User not found"));

    if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid credentials");
    }

    String token = jwtService.generateToken(user.getEmail());

    List<AuthResponse> authResponses = List.of(new AuthResponse(token, user.getName(), user.getEmail()));
    return authResponses;
  }
}