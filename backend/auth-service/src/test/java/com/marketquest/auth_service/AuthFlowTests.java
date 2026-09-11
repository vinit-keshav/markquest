package com.marketquest.auth_service;
import com.marketquest.auth_service.dto.*;
import com.marketquest.auth_service.entity.User;
import com.marketquest.auth_service.event.OtpEventProducer;
import com.marketquest.auth_service.repository.UserRepository;
import com.marketquest.auth_service.security.JwtService;
import com.marketquest.auth_service.service.AuthService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthFlowTests {
    UserRepository users=mock(UserRepository.class);
    OtpEventProducer producer=mock(OtpEventProducer.class);
    AuthService service=new AuthService(users,new BCryptPasswordEncoder(),mock(JwtService.class),producer);
    @Test void signupNormalizesEmailAndAllowsNoImage() {
        SignupRequest request=new SignupRequest(); request.setName(" Alice "); request.setEmail(" Alice@Example.com "); request.setPassword("password123");
        assertEquals("OTP queued for email delivery",service.signup(request,null));
        var saved=org.mockito.ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("alice@example.com",saved.getValue().getEmail());
        assertEquals("Alice",saved.getValue().getName());
        assertNull(saved.getValue().getFilename());
        assertTrue(new BCryptPasswordEncoder().matches("password123", saved.getValue().getPassword()));
        verify(producer).publishOtpRequested(eq("alice@example.com"), matches("[0-9]{6}"), eq("SIGNUP"));
    }
    @Test void rejectsMobileOnlySignupBeforeSavingAnything() {
        SignupRequest request=new SignupRequest(); request.setName("Alice"); request.setMobile("9999999999"); request.setPassword("password123");
        assertThrows(IllegalArgumentException.class,()->service.signup(request,null));
        verifyNoInteractions(users,producer);
    }
    @Test void expiredOtpDoesNotVerifyAccount() {
        User user=new User(); user.setOtpCode("123456"); user.setOtpExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        VerifyOtpRequest request=new VerifyOtpRequest(); request.setIdentifier("alice@example.com"); request.setOtp("123456");
        assertThrows(RuntimeException.class,()->service.verifyOtp(request));
        assertFalse(user.isVerified());
    }
    @Test void fiveIncorrectAttemptsBlockEvenCorrectCode() {
        User user=new User(); user.setOtpCode("123456"); user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        VerifyOtpRequest request=new VerifyOtpRequest(); request.setIdentifier("alice@example.com"); request.setOtp("000000");
        for(int i=0;i<5;i++) assertThrows(IllegalArgumentException.class,()->service.verifyOtp(request));
        request.setOtp("123456");
        assertThrows(IllegalArgumentException.class,()->service.verifyOtp(request));
        assertFalse(user.isVerified());
    }
}
