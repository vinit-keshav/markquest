package com.marketquest.trading.event;

import java.math.BigDecimal;
import java.time.Instant;

public class TradeExecutedEvent {
    private String tradeId;
    private String userId;
    private String symbol;
    private String side;
    private int quantity;
    private BigDecimal price;
    private String currency;
    private Instant executedAt;

    public TradeExecutedEvent() {
    }

    public TradeExecutedEvent(
            String tradeId,
            String userId,
            String symbol,
            String side,
            int quantity,
            BigDecimal price,
            String currency,
            Instant executedAt) {
        this.tradeId = tradeId;
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.executedAt = executedAt;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
