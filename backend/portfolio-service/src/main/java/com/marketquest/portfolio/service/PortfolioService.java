package com.marketquest.portfolio.service;

import com.marketquest.portfolio.dto.AccountResponse;
import com.marketquest.portfolio.dto.DailyProfitLossResponse;
import com.marketquest.portfolio.dto.HoldingResponse;
import com.marketquest.portfolio.dto.PortfolioSummaryResponse;
import com.marketquest.portfolio.dto.TradeHistoryResponse;
import com.marketquest.portfolio.entity.DemoAccount;
import com.marketquest.portfolio.entity.DemoHolding;
import com.marketquest.portfolio.entity.DemoTrade;
import com.marketquest.portfolio.event.TradeExecutedEvent;
import com.marketquest.portfolio.repository.DemoAccountRepository;
import com.marketquest.portfolio.repository.DemoHoldingRepository;
import com.marketquest.portfolio.repository.DemoTradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {
    private static final BigDecimal STARTING_INR_CASH = new BigDecimal("1000000.00");
    private static final BigDecimal STARTING_USD_CASH = new BigDecimal("10000.00");

    private final DemoAccountRepository accountRepository;
    private final DemoHoldingRepository holdingRepository;
    private final DemoTradeRepository tradeRepository;

    public PortfolioService(
            DemoAccountRepository accountRepository,
            DemoHoldingRepository holdingRepository,
            DemoTradeRepository tradeRepository) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public synchronized void applyTrade(TradeExecutedEvent event) {
        if (tradeRepository.existsByTradeId(event.getTradeId())) {
            return;
        }

        String userId = required(event.getUserId(), "userId");
        String symbol = required(event.getSymbol(), "symbol").toUpperCase(Locale.ROOT);
        String side = required(event.getSide(), "side").toUpperCase(Locale.ROOT);
        String currency = normalizeCurrency(event.getCurrency());
        BigDecimal price = event.getPrice();
        int quantity = event.getQuantity();

        ensureDefaultAccounts(userId);

        if (quantity <= 0 || price == null || price.signum() <= 0) {
            saveTrade(event, symbol, side, currency, "REJECTED", BigDecimal.ZERO, "Invalid quantity or price");
            return;
        }

        if ("BUY".equals(side)) {
            buy(event, userId, symbol, currency, quantity, price);
            return;
        }

        if ("SELL".equals(side)) {
            sell(event, userId, symbol, currency, quantity, price);
            return;
        }

        saveTrade(event, symbol, side, currency, "REJECTED", BigDecimal.ZERO, "Side must be BUY or SELL");
    }

    @Transactional
    public List<HoldingResponse> updateCurrentPrice(String userId, String symbol, BigDecimal currentPrice) {
        String normalizedUserId = required(userId, "userId");
        String normalizedSymbol = required(symbol, "symbol").toUpperCase(Locale.ROOT);

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw new IllegalArgumentException("currentPrice must be greater than zero");
        }

        DemoHolding holding = holdingRepository
                .findByUserIdAndSymbol(normalizedUserId, normalizedSymbol)
                .orElseThrow(() -> new IllegalArgumentException("No holding found for " + normalizedSymbol));
        holding.updateCurrentPrice(currentPrice);
        holdingRepository.save(holding);

        return getHoldings(normalizedUserId);
    }

    @Transactional
    public List<HoldingResponse> getHoldings(String userId) {
        String normalizedUserId = required(userId, "userId");
        ensureDefaultAccounts(normalizedUserId);
        return holdingRepository.findByUserIdOrderBySymbol(normalizedUserId)
                .stream()
                .map(this::toHoldingResponse)
                .toList();
    }

    @Transactional
    public PortfolioSummaryResponse getSummary(String userId) {
        String normalizedUserId = required(userId, "userId");
        ensureDefaultAccounts(normalizedUserId);

        List<AccountResponse> accounts = accountRepository.findByUserIdOrderByCurrency(normalizedUserId)
                .stream()
                .map(account -> new AccountResponse(account.getCurrency(), account.getCashBalance()))
                .toList();

        List<TradeHistoryResponse> trades = tradeRepository.findTop20ByUserIdOrderByExecutedAtDesc(normalizedUserId)
                .stream()
                .map(trade -> new TradeHistoryResponse(
                        trade.getTradeId(),
                        trade.getSymbol(),
                        trade.getSide(),
                        trade.getQuantity(),
                        trade.getPrice(),
                        trade.getCurrency(),
                        trade.getStatus(),
                        trade.getProfitLoss(),
                        trade.getMessage(),
                        trade.getExecutedAt()))
                .toList();

        return new PortfolioSummaryResponse(accounts, getHoldings(normalizedUserId), trades, getDailyProfitLoss(normalizedUserId));
    }

    @Transactional
    public PortfolioSummaryResponse depositCash(String userId, String currency, BigDecimal amount) {
        String normalizedUserId = required(userId, "userId");
        String normalizedCurrency = normalizeCurrency(currency);

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        ensureDefaultAccounts(normalizedUserId);
        DemoAccount account = getAccount(normalizedUserId, normalizedCurrency);
        account.setCashBalance(account.getCashBalance().add(amount));
        accountRepository.save(account);

        return getSummary(normalizedUserId);
    }

    private void buy(
            TradeExecutedEvent event,
            String userId,
            String symbol,
            String currency,
            int quantity,
            BigDecimal price) {
        DemoAccount account = getAccount(userId, currency);
        BigDecimal tradeValue = price.multiply(BigDecimal.valueOf(quantity));

        if (account.getCashBalance().compareTo(tradeValue) < 0) {
            saveTrade(event, symbol, "BUY", currency, "REJECTED", BigDecimal.ZERO, "Insufficient demo cash");
            return;
        }

        DemoHolding holding = holdingRepository
                .findByUserIdAndSymbol(userId, symbol)
                .orElseGet(() -> new DemoHolding(userId, symbol, currency));

        if (!holding.getCurrency().equals(currency)) {
            saveTrade(event, symbol, "BUY", currency, "REJECTED", BigDecimal.ZERO, "Currency does not match holding");
            return;
        }
        account.setCashBalance(account.getCashBalance().subtract(tradeValue));
        holding.buy(quantity, price);

        accountRepository.save(account);
        holdingRepository.save(holding);
        saveTrade(event, symbol, "BUY", currency, "EXECUTED", BigDecimal.ZERO, "Buy order executed");
    }

    private void sell(
            TradeExecutedEvent event,
            String userId,
            String symbol,
            String currency,
            int quantity,
            BigDecimal price) {
        DemoHolding holding = holdingRepository
                .findByUserIdAndSymbol(userId, symbol)
                .orElse(null);

        if (holding == null || holding.getQuantity() < quantity) {
            saveTrade(event, symbol, "SELL", currency, "REJECTED", BigDecimal.ZERO, "Insufficient holdings");
            return;
        }

        if (!holding.getCurrency().equals(currency)) {
            saveTrade(event, symbol, "SELL", currency, "REJECTED", BigDecimal.ZERO, "Currency does not match holding");
            return;
        }
        DemoAccount account = getAccount(userId, currency);
        BigDecimal tradeValue = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal profitLoss = price.subtract(holding.getAveragePrice()).multiply(BigDecimal.valueOf(quantity));

        holding.sell(quantity, price);
        account.setCashBalance(account.getCashBalance().add(tradeValue));

        holdingRepository.save(holding);
        accountRepository.save(account);
        saveTrade(event, symbol, "SELL", currency, "EXECUTED", profitLoss, "Sell order executed");
    }

    private DemoAccount getAccount(String userId, String currency) {
        return accountRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> accountRepository.save(new DemoAccount(userId, currency, startingCash(currency))));
    }

    private void ensureDefaultAccounts(String userId) {
        accountRepository.findByUserIdAndCurrency(userId, "INR")
                .orElseGet(() -> accountRepository.save(new DemoAccount(userId, "INR", STARTING_INR_CASH)));
        accountRepository.findByUserIdAndCurrency(userId, "USD")
                .orElseGet(() -> accountRepository.save(new DemoAccount(userId, "USD", STARTING_USD_CASH)));
    }

    private BigDecimal startingCash(String currency) {
        if ("USD".equals(currency)) {
            return STARTING_USD_CASH;
        }
        return STARTING_INR_CASH;
    }

    private void saveTrade(
            TradeExecutedEvent event,
            String symbol,
            String side,
            String currency,
            String status,
            BigDecimal profitLoss,
            String message) {
        DemoTrade trade = new DemoTrade(
                event.getTradeId(),
                event.getUserId(),
                symbol,
                side,
                event.getQuantity(),
                event.getPrice() == null ? BigDecimal.ZERO : event.getPrice(),
                currency,
                status,
                profitLoss == null ? BigDecimal.ZERO : profitLoss,
                message,
                event.getExecutedAt() == null ? Instant.now() : event.getExecutedAt());
        tradeRepository.save(trade);
    }

    private List<DailyProfitLossResponse> getDailyProfitLoss(String userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("Asia/Kolkata"));
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        tradeRepository.findByUserIdAndStatusOrderByExecutedAtAsc(userId, "EXECUTED")
                .stream()
                .filter(trade -> "SELL".equalsIgnoreCase(trade.getSide()))
                .forEach(trade -> {
                    String key = formatter.format(trade.getExecutedAt()) + "|" + trade.getCurrency();
                    totals.merge(key, trade.getProfitLoss(), BigDecimal::add);
                });

        return totals.entrySet()
                .stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", 2);
                    return new DailyProfitLossResponse(parts[0], parts[1], entry.getValue());
                })
                .toList();
    }

    private HoldingResponse toHoldingResponse(DemoHolding holding) {
        BigDecimal investedValue = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal currentValue = holding.getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal unrealizedProfitLoss = currentValue.subtract(investedValue);
        BigDecimal totalProfitLoss = holding.getRealizedProfitLoss().add(unrealizedProfitLoss);

        return new HoldingResponse(
                holding.getSymbol(),
                holding.getQuantity(),
                holding.getCurrency(),
                holding.getAveragePrice(),
                holding.getCurrentPrice(),
                investedValue,
                currentValue,
                unrealizedProfitLoss,
                holding.getRealizedProfitLoss(),
                totalProfitLoss);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "INR";
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("INR") && !normalized.equals("USD")) throw new IllegalArgumentException("Currency must be INR or USD");
        return normalized;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
