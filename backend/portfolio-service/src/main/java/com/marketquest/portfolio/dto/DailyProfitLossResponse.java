package com.marketquest.portfolio.dto;

import java.math.BigDecimal;

public class DailyProfitLossResponse {
    private String date;
    private String currency;
    private BigDecimal profitLoss;

    public DailyProfitLossResponse(String date, String currency, BigDecimal profitLoss) {
        this.date = date;
        this.currency = currency;
        this.profitLoss = profitLoss;
    }

    public String getDate() {
        return date;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }
}
