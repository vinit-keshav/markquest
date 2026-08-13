package com.marketquest.trading.controller;

import com.marketquest.trading.dto.InstrumentResponse;
import com.marketquest.trading.dto.LivePriceResponse;
import com.marketquest.trading.service.InstrumentService;
import com.marketquest.trading.service.LivePriceService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {
    private final InstrumentService instrumentService;
    private final LivePriceService livePriceService;

    public InstrumentController(InstrumentService instrumentService, LivePriceService livePriceService) {
        this.instrumentService = instrumentService;
        this.livePriceService = livePriceService;
    }

    @GetMapping
    public List<InstrumentResponse> search(
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String query)
    {
        return instrumentService.search(market, query);
    }

    @GetMapping("/{symbol}/price")
    public LivePriceResponse price(@org.springframework.web.bind.annotation.PathVariable String symbol) {
        return livePriceService.getPrice(instrumentService.findBySymbol(symbol));
    }
}
