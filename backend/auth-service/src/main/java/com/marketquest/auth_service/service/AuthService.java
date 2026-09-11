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
import java.security.SecureRandom;
import java.util.UUID;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service

public class AuthService{
    private static final SecureRandom RANDOM = new SecureRandom();
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
    if (request.getName() == null || request.getName().isBlank() || request.getName().length() > 100) throw new IllegalArgumentException("Name is required (max 100 characters)");
    if (request.getPassword() == null || request.getPassword().length() < 8 || request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Password must be at least 8 characters and at most 72 bytes");
    if (request.getEmail() == null || !request.getEmail().trim().matches("[^\\s@]+@[^\\s@]+[.][^\\s@]+")) throw new IllegalArgumentException("A valid email is required; SMS delivery is not implemented");
    request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
    request.setName(request.getName().trim());
    boolean emailMissing = request.getEmail() == null || request.getEmail().isBlank();
    boolean mobileMissing = request.getMobile() == null || request.getMobile().isBlank();
    if (emailMissing && mobileMissing) {
            throw new IllegalArgumentException("Email or mobile number is required");
        }


        if (!emailMissing) {
            var existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                if (existingUser.get().isVerified()) {
                    throw new IllegalArgumentException("Email already in use");
                }
                return resendOtp(existingUser.get());
            }
        }
        if (!mobileMissing) {
            var existingUser = userRepository.findByMobile(request.getMobile());
            if (existingUser.isPresent()) {
                if (existingUser.get().isVerified()) {
                    throw new IllegalArgumentException("Mobile number already in use");
                }
                return resendOtp(existingUser.get());
            }
        }

        try{
          String fileName = null;
          if (file != null && !file.isEmpty()) {
              if (file.getSize() > 2 * 1024 * 1024) throw new IllegalArgumentException("Profile image must be at most 2 MB");
              String type = file.getContentType();
              if (!"image/jpeg".equals(type) && !"image/png".equals(type)) throw new IllegalArgumentException("Use a JPEG or PNG profile image");
              java.awt.image.BufferedImage image;
              try (var input = file.getInputStream()) { image = ImageIO.read(input); }
              if (image == null || image.getWidth() > 4096 || image.getHeight() > 4096) throw new IllegalArgumentException("Invalid profile image or dimensions above 4096 pixels");
              Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
              Files.createDirectories(uploadRoot);
              String format = "image/png".equals(type) ? "png" : "jpg";
              fileName = UUID.randomUUID() + "." + format;
              ImageIO.write(image, format, uploadRoot.resolve(fileName).toFile());
          }

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
     return "OTP queued for email delivery";
  }
  catch(java.io.IOException e) {
     throw new IllegalStateException("Profile image could not be saved", e);
  }
}

  public String resendOtp(String identifier) {
    User user = findByIdentifier(identifier);

    if (user.isVerified()) {
        throw new IllegalArgumentException("Account is already verified");
    }

    return resendOtp(user);
  }

  private String resendOtp(User user) {
    if (user.getOtpExpiresAt() != null && user.getOtpExpiresAt().minusMinutes(10).plusSeconds(60).isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Wait 60 seconds before requesting another OTP");
    user.setOtpAttempts(0);
    String otp = generateOtp();
    user.setOtpCode(otp);
    user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
    userRepository.save(user);
    otpEventProducer.publishOtpRequested(getUserIdentifier(user), otp, "RESEND");
    return "OTP queued for email delivery";
  }

public String verifyOtp(VerifyOtpRequest request){
   User user = findByIdentifier(request.getIdentifier());

   if (user.getOtpAttempts() >= 5) throw new IllegalArgumentException("Too many attempts. Request a new OTP");
   if(user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtp())){
    user.setOtpAttempts(user.getOtpAttempts() + 1);
    userRepository.save(user);
    throw new IllegalArgumentException("Invalid OTP");
   }

           if (user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
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
            throw new IllegalArgumentException("Please verify OTP before login");
        }


        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
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
        throw new IllegalArgumentException("Current password is incorrect");
    }

    if (request.getNewPassword() == null || request.getNewPassword().length() < 8 || request.getNewPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
        throw new IllegalArgumentException("New password must be at least 8 characters and at most 72 bytes");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    return "Password updated successfully";
  }

  private User findByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
        throw new IllegalArgumentException("Email or mobile number is required");
    }

    String normalized = identifier.trim();
    if (normalized.contains("@")) normalized = normalized.toLowerCase(Locale.ROOT);
    final String lookup = normalized;
    return userRepository.findByEmail(lookup)
    .or(() -> userRepository.findByMobile(lookup))
    .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  private String generateOtp() {
    return String.valueOf(RANDOM.nextInt(900000) + 100000);
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
