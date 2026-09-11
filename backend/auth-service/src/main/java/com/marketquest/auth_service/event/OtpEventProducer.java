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
        try {
            kafkaTemplate.send(otpRequestedTopic, identifier, event).get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Message submission interrupted", ex);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Message broker unavailable; submission could not be confirmed", ex);
        }
    }
}
