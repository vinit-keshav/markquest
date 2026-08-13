package com.marketquest.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "demo_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "currency"}))
public class DemoAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public DemoAccount() {
    }

    public DemoAccount(String userId, String currency, BigDecimal cashBalance) {
        this.userId = userId;
        this.currency = currency;
        this.cashBalance = cashBalance;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
