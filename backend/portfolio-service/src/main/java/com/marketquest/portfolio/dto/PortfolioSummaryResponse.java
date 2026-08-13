package com.marketquest.portfolio.dto;

import java.util.List;

public class PortfolioSummaryResponse {
    private List<AccountResponse> accounts;
    private List<HoldingResponse> holdings;
    private List<TradeHistoryResponse> trades;

    public PortfolioSummaryResponse(
            List<AccountResponse> accounts,
            List<HoldingResponse> holdings,
            List<TradeHistoryResponse> trades) {
        this.accounts = accounts;
        this.holdings = holdings;
        this.trades = trades;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }

    public List<HoldingResponse> getHoldings() {
        return holdings;
    }

    public List<TradeHistoryResponse> getTrades() {
        return trades;
    }
}
