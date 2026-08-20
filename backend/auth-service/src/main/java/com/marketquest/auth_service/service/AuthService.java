package com.marketquest.auth_service.service;
import com.marketquest.auth_service.event.OtpEventProducer;
import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.entity.User;
import com.marketquest.auth_service.repository.UserRepository;
import com.marketquest.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service

public class AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpEventProducer otpEventProducer;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpEventProducer otpEventProducer
          ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpEventProducer = otpEventProducer;

    }
  
  public String signup(SignupRequest request, MultipartFile file) {
    boolean emailMissing = request.getEmail() == null || request.getEmail().isBlank();
    boolean mobileMissing = request.getMobile() == null || request.getMobile().isBlank();
    if (emailMissing && mobileMissing) {
            throw new RuntimeException("Email or mobile number is required");
        }
    if (file==null || file.isEmpty()) {
      throw new RuntimeException("File is required");
    }

        if (!emailMissing) {
            var existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                if (existingUser.get().isVerified()) {
                    throw new RuntimeException("Email already in use");
                }
                return resendOtp(existingUser.get());
            }
        }

        if (!mobileMissing) {
            var existingUser = userRepository.findByMobile(request.getMobile());
            if (existingUser.isPresent()) {
                if (existingUser.get().isVerified()) {
                    throw new RuntimeException("Mobile number already in use");
                }
                return resendOtp(existingUser.get());
            }
        }

        try{
          String uploadDir = "uploads/";

          File folder = new File(uploadDir);
          if(!folder.exists()) {
            folder.mkdirs();
          }

          String originalFileName = file.getOriginalFilename();
          String fileName = System.currentTimeMillis() + "_" + originalFileName;

          Path filePath = Paths.get(uploadDir + fileName);
          Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        
     String otp = generateOtp();

     User user = new User();
     user.setName(request.getName());
     user.setEmail(emailMissing?null:request.getEmail());
     user.setMobile(mobileMissing?null:request.getMobile());
     user.setPassword(passwordEncoder.encode(request.getPassword()));
     user.setOtpCode(otp);
     user.setVerified(false);
     user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
     user.setFilename(fileName);
     userRepository.save(user);
     otpEventProducer.publishOtpRequested(getUserIdentifier(user), otp, "SIGNUP");
     return "OTP sent successfully";
  }
  catch(Exception e) {
     throw new RuntimeException("Data save failed" + e.getMessage());
  }
}

  public String resendOtp(String identifier) {
    User user = findByIdentifier(identifier);

    if (user.isVerified()) {
        throw new RuntimeException("Account is already verified");
    }

    return resendOtp(user);
  }

  private String resendOtp(User user) {
    String otp = generateOtp();
    user.setOtpCode(otp);
    user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
    userRepository.save(user);
    otpEventProducer.publishOtpRequested(getUserIdentifier(user), otp, "RESEND");
    return "OTP resent successfully";
  }

public String verifyOtp(VerifyOtpRequest request){
   User user = findByIdentifier(request.getIdentifier());

   if(user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtp())){
    throw new RuntimeException("Invalid OTP");
   }

           if (user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        return "Account verified successfully";

}
  public List<AuthResponse> login(LoginRequest request) {
    User user = findByIdentifier(getLoginIdentifier(request));
  
            if (!user.isVerified()) {
            throw new RuntimeException("Please verify OTP before login");
        }


        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }


    String tokenSubject = getUserIdentifier(user);
    

    String token = jwtService.generateToken(tokenSubject);

    List<AuthResponse> authResponses = List.of(
        new AuthResponse(token, user.getName(), user.getEmail(), user.getMobile(), user.getFilename())
    );
    return authResponses;
  }

  public String resetPassword(PasswordResetRequest request) {
    User user = findByIdentifier(request.getIdentifier());

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
    }

    if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
        throw new RuntimeException("New password must be at least 6 characters");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    return "Password updated successfully";
  }

  private User findByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
        throw new RuntimeException("Email or mobile number is required");
    }

    return userRepository.findByEmail(identifier)
    .or(() -> userRepository.findByMobile(identifier))
    .orElseThrow(() -> new RuntimeException("User not found"));
  }

  private String generateOtp() {
    return String.valueOf(new Random().nextInt(900000) + 100000);
  }

  private String getLoginIdentifier(LoginRequest request) {
    if (request.getEmail() != null && !request.getEmail().isBlank()) {
        return request.getEmail();
    }
    return request.getMobile();
  }

  private String getUserIdentifier(User user) {
    return user.getEmail()!= null?user.getEmail():user.getMobile();
  }
}
