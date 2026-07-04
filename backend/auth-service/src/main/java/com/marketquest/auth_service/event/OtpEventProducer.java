package com.marketquest.auth_service.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OtpEventProducer {
    private final KafkaTemplate<String, OtpRequestedEvent> kafkaTemplate;
    private final String otpRequestedTopic;

    public OtpEventProducer(
            KafkaTemplate<String, OtpRequestedEvent> kafkaTemplate,
            @Value("${marketquest.kafka.topic.otp-requested}") String otpRequestedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.otpRequestedTopic = otpRequestedTopic;
    }

    public void publishOtpRequested(String identifier, String otp, String purpose) {
        OtpRequestedEvent event = new OtpRequestedEvent(identifier, otp, purpose);
        kafkaTemplate.send(otpRequestedTopic, identifier, event);
    }
}
