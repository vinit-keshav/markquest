package com.marketquest.trading.dto;

import java.math.BigDecimal;

public class InstrumentResponse {
    private String symbol;
    private String name;
    private String market;
    private String exchange;
    private BigDecimal referencePrice;
    private String currency;

    public InstrumentResponse(
            String symbol,
            String name,
            String market,
            String exchange,
            BigDecimal referencePrice,
            String currency) {
        this.symbol = symbol;
        this.name = name;
        this.market = market;
        this.exchange = exchange;
        this.referencePrice = referencePrice;
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getMarket() {
        return market;
    }

    public String getExchange() {
        return exchange;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public String getCurrency() {
        return currency;
    }
}
