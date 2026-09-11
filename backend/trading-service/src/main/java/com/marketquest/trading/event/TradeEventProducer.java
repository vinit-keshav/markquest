package com.marketquest.trading.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradeEventProducer {
    private final KafkaTemplate<String, TradeExecutedEvent> kafkaTemplate;
    private final String tradeExecutedTopic;

    public TradeEventProducer(
            KafkaTemplate<String, TradeExecutedEvent> kafkaTemplate,
            @Value("${marketquest.kafka.topic.trade-executed}") String tradeExecutedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.tradeExecutedTopic = tradeExecutedTopic;
    }

    public void publishTradeExecuted(TradeExecutedEvent event) {
        try {
            kafkaTemplate.send(tradeExecutedTopic, event.getUserId(), event).get(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Message submission interrupted", ex);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Message broker unavailable; submission could not be confirmed", ex);
        }
    }
}
