package com.marketquest.portfolio.event;

import com.marketquest.portfolio.service.PortfolioService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TradeEventConsumer {
    private final PortfolioService portfolioService;

    public TradeEventConsumer(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @KafkaListener(topics = "${marketquest.kafka.topic.trade-executed}", autoStartup = "${spring.kafka.listener.auto-startup:true}")
    public void consumeTradeExecuted(TradeExecutedEvent event) {
        portfolioService.applyTrade(event);
        System.out.println("Applied trade " + event.getTradeId() + " for " + event.getUserId());
    }
}
