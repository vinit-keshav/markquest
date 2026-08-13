package com.marketquest.portfolio.dto;

import java.math.BigDecimal;

public class PriceUpdateRequest {
    private String symbol;
    private BigDecimal currentPrice;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
}
