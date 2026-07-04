package com.marketquest.auth_service.controller;

import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
   private final AuthService authService;

   public AuthController(AuthService authService) {
         this.authService = authService;
   } 

   @PostMapping(value="/signup",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public String signup(@RequestPart("request") SignupRequest request, @RequestPart("file") MultipartFile file) {
      return authService.signup(request, file);
   }

   @PostMapping("/verify-otp")
   public String verifyOtp(@RequestBody VerifyOtpRequest request) {
      return authService.verifyOtp(request);
   }

   @PostMapping("/resend-otp")
   public String resendOtp(@RequestBody VerifyOtpRequest request) {
      return authService.resendOtp(request.getIdentifier());
   }


   @PostMapping("/login")
   public List<AuthResponse> login(@RequestBody LoginRequest request){
        return authService.login(request);
   }
}
