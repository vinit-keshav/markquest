package com.marketquest.portfolio;
import com.marketquest.portfolio.service.PortfolioService;
import com.marketquest.portfolio.event.TradeExecutedEvent;
import com.marketquest.portfolio.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PortfolioSettlementTests {
    @Autowired PortfolioService service;
    @Autowired DemoAccountRepository accounts;
    @Autowired DemoHoldingRepository holdings;
    @Autowired DemoTradeRepository trades;
    @Autowired org.springframework.web.context.WebApplicationContext context;
    TradeExecutedEvent order(String user, String side, int quantity, String price, String currency) {
        TradeExecutedEvent event = new TradeExecutedEvent();
        event.setTradeId(UUID.randomUUID().toString()); event.setUserId(user); event.setSymbol("AAPL");
        event.setSide(side); event.setQuantity(quantity); event.setPrice(new BigDecimal(price));
        event.setCurrency(currency); event.setExecutedAt(Instant.now());
        return event;
    }
    @Test void buySellAndReplayPreserveCashAndProfit() {
        String user=UUID.randomUUID().toString();
        var buy=order(user,"BUY",10,"100","USD");
        service.applyTrade(buy); service.applyTrade(buy);
        assertEquals(0, new BigDecimal("9000").compareTo(accounts.findByUserIdAndCurrency(user,"USD").orElseThrow().getCashBalance()));
        assertEquals(10, holdings.findByUserIdAndSymbol(user,"AAPL").orElseThrow().getQuantity());
        service.applyTrade(order(user,"SELL",4,"120","USD"));
        assertEquals(0, new BigDecimal("9480").compareTo(accounts.findByUserIdAndCurrency(user,"USD").orElseThrow().getCashBalance()));
        var holding=holdings.findByUserIdAndSymbol(user,"AAPL").orElseThrow();
        assertEquals(6, holding.getQuantity());
        assertEquals(0, new BigDecimal("80").compareTo(holding.getRealizedProfitLoss()));
    }
    @Test void rejectsInsufficientCashOversellingAndCurrencyMismatch() {
        String user=UUID.randomUUID().toString();
        service.applyTrade(order(user,"BUY",1000,"100","USD"));
        assertTrue(holdings.findByUserIdAndSymbol(user,"AAPL").isEmpty());
        service.applyTrade(order(user,"BUY",1,"100","USD"));
        service.applyTrade(order(user,"SELL",2,"100","USD"));
        service.applyTrade(order(user,"SELL",1,"100","INR"));
        assertEquals(1,holdings.findByUserIdAndSymbol(user,"AAPL").orElseThrow().getQuantity());
        assertEquals(3,trades.findTop20ByUserIdOrderByExecutedAtDesc(user).stream().filter(t->"REJECTED".equals(t.getStatus())).count());
        assertThrows(IllegalArgumentException.class, () -> service.depositCash(user,"EUR",BigDecimal.ONE));
    }
    @Test void apiRejectsAccessToAnotherPortfolio() throws Exception {
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
            .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/portfolio/bob/summary")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(j->j.subject("alice"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/portfolio/bob/deposit")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt().jwt(j->j.subject("alice")))
            .contentType("application/json").content("{\"currency\":\"USD\",\"amount\":100}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @Test void staleBalanceCannotOverwriteAnotherDeposit() {
        String user=UUID.randomUUID().toString();
        service.getSummary(user);
        var first=accounts.findByUserIdAndCurrency(user,"USD").orElseThrow();
        var stale=accounts.findByUserIdAndCurrency(user,"USD").orElseThrow();
        first.setCashBalance(first.getCashBalance().add(BigDecimal.TEN));
        accounts.saveAndFlush(first);
        stale.setCashBalance(stale.getCashBalance().add(BigDecimal.ONE));
        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () -> accounts.saveAndFlush(stale));
    }
}
