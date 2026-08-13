package com.marketquest.trading.dto;

public class TradeResponse {
    private String tradeId;
    private String status;
    private String message;

    public TradeResponse(String tradeId, String status, String message) {
        this.tradeId = tradeId;
        this.status = status;
        this.message = message;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
