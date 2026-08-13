package com.marketquest.trading.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class LivePriceResponse {
    private String symbol;
    private BigDecimal price;
    private String currency;
    private boolean live;
    private Instant updatedAt;

    public LivePriceResponse(String symbol, BigDecimal price, String currency, boolean live, Instant updatedAt) {
        this.symbol = symbol;
        this.price = price;
        this.currency = currency;
        this.live = live;
        this.updatedAt = updatedAt;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isLive() {
        return live;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
