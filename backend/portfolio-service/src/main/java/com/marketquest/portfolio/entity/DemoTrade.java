package com.marketquest.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "demo_trades")
public class DemoTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tradeId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String side;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(precision = 19, scale = 4)
    private BigDecimal profitLoss;

    private String message;

    @Column(nullable = false)
    private Instant executedAt;

    public DemoTrade() {
    }

    public DemoTrade(
            String tradeId,
            String userId,
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
        this.userId = userId;
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

    public Long getId() {
        return id;
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

    public String getStatus() {
        return status;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss == null ? BigDecimal.ZERO : profitLoss;
    }

    public String getMessage() {
        return message;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
