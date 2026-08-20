package com.marketquest.auth_service.controller;

import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

   @PostMapping("/reset-password")
   public String resetPassword(@RequestBody PasswordResetRequest request) {
      return authService.resetPassword(request);
   }

   @GetMapping("/uploads/{filename}")
   public ResponseEntity<Resource> profileImage(@PathVariable String filename) throws Exception {
      Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
      Path path = uploadRoot.resolve(filename).normalize();
      if (!path.startsWith(uploadRoot)) {
         return ResponseEntity.badRequest().build();
      }

      Resource resource = new UrlResource(path.toUri());

      if (!resource.exists() || !resource.isReadable()) {
         return ResponseEntity.notFound().build();
      }

      String contentType = Files.probeContentType(path);
      MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
      return ResponseEntity.ok().contentType(mediaType).body(resource);
   }
}
