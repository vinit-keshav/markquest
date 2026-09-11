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

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{userId}")
    public List<HoldingResponse> getHoldings(@PathVariable String userId, java.security.Principal principal) {
        requireOwner(userId, principal);
        return portfolioService.getHoldings(userId);
    }

    @GetMapping("/{userId}/summary")
    public PortfolioSummaryResponse getSummary(@PathVariable String userId, java.security.Principal principal) {
        requireOwner(userId, principal);
        return portfolioService.getSummary(userId);
    }

    @PostMapping("/{userId}/deposit")
    public PortfolioSummaryResponse depositCash(
            @PathVariable String userId, java.security.Principal principal,
            @RequestBody DepositRequest request) {
        requireOwner(userId, principal);
        return portfolioService.depositCash(userId, request.getCurrency(), request.getAmount());
    }

    @PostMapping("/{userId}/prices")
    public List<HoldingResponse> updateCurrentPrice(
            @PathVariable String userId, java.security.Principal principal,
            @RequestBody PriceUpdateRequest request) {
        requireOwner(userId, principal);
        return portfolioService.updateCurrentPrice(userId, request.getSymbol(), request.getCurrentPrice());
    }
    private void requireOwner(String userId, java.security.Principal principal) {
        if (!principal.getName().equals(userId)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "This portfolio belongs to another account");
    }
}
