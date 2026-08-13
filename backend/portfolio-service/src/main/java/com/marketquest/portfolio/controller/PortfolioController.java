package com.marketquest.portfolio.controller;

import com.marketquest.portfolio.dto.DepositRequest;
import com.marketquest.portfolio.dto.HoldingResponse;
import com.marketquest.portfolio.dto.PriceUpdateRequest;
import com.marketquest.portfolio.dto.PortfolioSummaryResponse;
import com.marketquest.portfolio.service.PortfolioService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{userId}")
    public List<HoldingResponse> getHoldings(@PathVariable String userId) {
        return portfolioService.getHoldings(userId);
    }

    @GetMapping("/{userId}/summary")
    public PortfolioSummaryResponse getSummary(@PathVariable String userId) {
        return portfolioService.getSummary(userId);
    }

    @PostMapping("/{userId}/deposit")
    public PortfolioSummaryResponse depositCash(
            @PathVariable String userId,
            @RequestBody DepositRequest request) {
        return portfolioService.depositCash(userId, request.getCurrency(), request.getAmount());
    }

    @PostMapping("/{userId}/prices")
    public List<HoldingResponse> updateCurrentPrice(
            @PathVariable String userId,
            @RequestBody PriceUpdateRequest request) {
        return portfolioService.updateCurrentPrice(userId, request.getSymbol(), request.getCurrentPrice());
    }
}
