package com.marketquest.auth_service.controller;

import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
   private final AuthService authService;

   public AuthController(AuthService authService) {
         this.authService = authService;
   } 

   @PostMapping("/signup")
   public String signup(@RequestBody SignupRequest request) {
      return authService.signup(request);
   }

   @PostMapping("/login")
   public List<AuthResponse> login(@RequestBody LoginRequest request){
        return authService.login(request);
   }
}