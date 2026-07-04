package com.marketquest.notification_service.service;

import com.marketquest.notification_service.event.OtpRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public OtpEmailService(
            JavaMailSender mailSender,
            @Value("${marketquest.otp.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtpEmail(OtpRequestedEvent event) {
        String identifier = event.getIdentifier();
        if (identifier == null || !identifier.contains("@")) {
            System.out.println("OTP email skipped because identifier is not an email: " + identifier);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(identifier);
        message.setSubject("Your MarketQuest AI OTP");
        message.setText("Your OTP is " + event.getOtp() + ". It is valid for 10 minutes.");

        mailSender.send(message);
        System.out.println("OTP email sent to " + identifier + " for " + event.getPurpose());
    }
}
