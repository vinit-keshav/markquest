package com.marketquest.watchlist_service.dto;

public class WatchlistRequest {
    private String symbol;
    private String companyName;
    private String note;


    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

}