package com.marketquest.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TradeHistoryResponse {
    private String tradeId;
    private String symbol;
    private String side;
    private int quantity;
    private BigDecimal price;
    private String currency;
    private String status;
    private BigDecimal profitLoss;
    private String message;
    private Instant executedAt;

    public TradeHistoryResponse(
            String tradeId,
            String symbol,
            String side,
            int quantity,
            BigDecimal price,
            String currency,
            String status,
            BigDecimal profitLoss,
            String message,
            Instant executedAt) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.profitLoss = profitLoss;
        this.message = message;
        this.executedAt = executedAt;
    }

    public String getTradeId() {
        return tradeId;
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

    public String getStatus() {
        return status;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public String getMessage() {
        return message;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
