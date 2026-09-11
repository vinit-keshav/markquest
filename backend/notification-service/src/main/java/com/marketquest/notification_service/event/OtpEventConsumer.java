package com.marketquest.notification_service.event;

import com.marketquest.notification_service.service.OtpEmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OtpEventConsumer {
    private final OtpEmailService otpEmailService;

    public OtpEventConsumer(OtpEmailService otpEmailService) {
        this.otpEmailService = otpEmailService;
    }

    @KafkaListener(topics = "${marketquest.kafka.topic.otp-requested}", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void consumeOtpRequestedEvent(OtpRequestedEvent event) {
        System.out.println("OTP event received for " + event.getIdentifier());
        otpEmailService.sendOtpEmail(event);
    }
}
