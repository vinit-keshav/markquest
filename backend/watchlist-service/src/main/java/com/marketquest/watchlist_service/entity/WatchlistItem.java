package com.marketquest.watchlist_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    private String symbol;
    private String companyName;
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    } 

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getNote() {
        return note;
    }   

    public void setNote(String note) {
        this.note = note;
    }

}