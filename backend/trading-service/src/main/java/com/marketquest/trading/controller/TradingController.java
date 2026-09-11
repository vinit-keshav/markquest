package com.marketquest.trading.controller;

import com.marketquest.trading.dto.TradeRequest;
import com.marketquest.trading.dto.TradeResponse;
import com.marketquest.trading.service.TradingService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradingController {
    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.ACCEPTED)
    @PostMapping
    public TradeResponse executeTrade(@RequestBody TradeRequest request, java.security.Principal principal) {
        request.setUserId(principal.getName());
        return tradingService.executeTrade(request);
    }
}
