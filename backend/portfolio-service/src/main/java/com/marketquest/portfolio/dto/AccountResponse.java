package com.marketquest.portfolio.dto;

import java.math.BigDecimal;

public class AccountResponse {
    private String currency;
    private BigDecimal cashBalance;

    public AccountResponse(String currency, BigDecimal cashBalance) {
        this.currency = currency;
        this.cashBalance = cashBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }
}
