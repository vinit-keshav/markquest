package com.marketquest.portfolio.dto;

import java.math.BigDecimal;

public class HoldingResponse {
    private String symbol;
    private int quantity;
    private String currency;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal investedValue;
    private BigDecimal currentValue;
    private BigDecimal unrealizedProfitLoss;
    private BigDecimal realizedProfitLoss;
    private BigDecimal totalProfitLoss;

    public HoldingResponse(
            String symbol,
            int quantity,
            String currency,
            BigDecimal averagePrice,
            BigDecimal currentPrice,
            BigDecimal investedValue,
            BigDecimal currentValue,
            BigDecimal unrealizedProfitLoss,
            BigDecimal realizedProfitLoss,
            BigDecimal totalProfitLoss) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.currency = currency;
        this.averagePrice = averagePrice;
        this.currentPrice = currentPrice;
        this.investedValue = investedValue;
        this.currentValue = currentValue;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.realizedProfitLoss = realizedProfitLoss;
        this.totalProfitLoss = totalProfitLoss;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getInvestedValue() {
        return investedValue;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public BigDecimal getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }
}
