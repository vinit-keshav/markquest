package com.marketquest.trading.service;

import com.marketquest.trading.dto.TradeRequest;
import com.marketquest.trading.dto.TradeResponse;
import com.marketquest.trading.event.TradeEventProducer;
import com.marketquest.trading.event.TradeExecutedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TradingService {
    private final TradeEventProducer tradeEventProducer;
    private final InstrumentService instrumentService;
    private final LivePriceService livePriceService;

    public TradingService(
            TradeEventProducer tradeEventProducer,
            InstrumentService instrumentService,
            LivePriceService livePriceService) {
        this.tradeEventProducer = tradeEventProducer;
        this.instrumentService = instrumentService;
        this.livePriceService = livePriceService;
    }

    public TradeResponse executeTrade(TradeRequest request) {
        validate(request);
        BigDecimal executionPrice = livePriceService
                .getPrice(instrumentService.findBySymbol(request.getSymbol()))
                .getPrice();

        String tradeId = UUID.randomUUID().toString();
        TradeExecutedEvent event = new TradeExecutedEvent(
                tradeId,
                request.getUserId().trim(),
                request.getSymbol().trim().toUpperCase(Locale.ROOT),
                request.getSide().trim().toUpperCase(Locale.ROOT),
                request.getQuantity(),
                executionPrice,
                instrumentService.findBySymbol(request.getSymbol()).getCurrency(),
                Instant.now());

        tradeEventProducer.publishTradeExecuted(event);
        return new TradeResponse(tradeId, "PENDING", "Order accepted; portfolio will determine execution or rejection");
    }

    private void validate(TradeRequest request) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (!instrumentService.exists(request.getSymbol())) {
            throw new IllegalArgumentException("symbol is not available in the demo market list");
        }
        if (request.getSide() == null || request.getSide().isBlank()) {
            throw new IllegalArgumentException("side is required");
        }
        String side = request.getSide().trim().toUpperCase(Locale.ROOT);
        if (!side.equals("BUY") && !side.equals("SELL")) {
            throw new IllegalArgumentException("side must be BUY or SELL");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (request.getQuantity() > 1000000) throw new IllegalArgumentException("quantity must not exceed 1000000");
    }

}
