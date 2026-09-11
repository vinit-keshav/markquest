package com.marketquest.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(
        name = "demo_holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class DemoHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long version;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal realizedProfitLoss = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public DemoHolding() {
    }

    public DemoHolding(String userId, String symbol, String currency) {
        this.userId = userId;
        this.symbol = symbol;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
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

    public BigDecimal getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void buy(int buyQuantity, BigDecimal buyPrice) {
        BigDecimal existingCost = averagePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal newCost = buyPrice.multiply(BigDecimal.valueOf(buyQuantity));
        if (buyQuantity <= 0 || buyPrice == null || buyPrice.signum() <= 0) throw new IllegalArgumentException("Invalid buy quantity or price");
        quantity = Math.addExact(quantity, buyQuantity);
        averagePrice = existingCost.add(newCost)
                .divide(BigDecimal.valueOf(quantity), 4, RoundingMode.HALF_UP);
        currentPrice = buyPrice;
        updatedAt = Instant.now();
    }

    public void sell(int sellQuantity, BigDecimal sellPrice) {
        if (sellQuantity <= 0 || sellQuantity > quantity || sellPrice == null || sellPrice.signum() <= 0) throw new IllegalArgumentException("Invalid sell quantity or price");
        BigDecimal profitLoss = sellPrice.subtract(averagePrice)
                .multiply(BigDecimal.valueOf(sellQuantity));
        realizedProfitLoss = realizedProfitLoss.add(profitLoss);
        quantity -= sellQuantity;
        currentPrice = sellPrice;

        if (quantity == 0) {
            averagePrice = BigDecimal.ZERO;
        }

        updatedAt = Instant.now();
    }

    public void updateCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        this.updatedAt = Instant.now();
    }
}
