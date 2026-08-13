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
        kafkaTemplate.send(tradeExecutedTopic, event.getUserId(), event);
    }
}
