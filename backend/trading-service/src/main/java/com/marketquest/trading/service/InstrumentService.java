package com.marketquest.trading.service;

import com.marketquest.trading.dto.InstrumentResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class InstrumentService {
    private final List<InstrumentResponse> instruments = List.of(
            stock("RELIANCE", "Reliance Industries", "INDIA", "NSE", "2860.00", "INR"),
            stock("TCS", "Tata Consultancy Services", "INDIA", "NSE", "3800.00", "INR"),
            stock("INFY", "Infosys", "INDIA", "NSE", "1500.00", "INR"),
            stock("HDFCBANK", "HDFC Bank", "INDIA", "NSE", "1680.00", "INR"),
            stock("ICICIBANK", "ICICI Bank", "INDIA", "NSE", "1120.00", "INR"),
            stock("SBIN", "State Bank of India", "INDIA", "NSE", "820.00", "INR"),
            stock("ITC", "ITC", "INDIA", "NSE", "430.00", "INR"),
            stock("LT", "Larsen and Toubro", "INDIA", "NSE", "3600.00", "INR"),
            stock("TATAMOTORS", "Tata Motors", "INDIA", "NSE", "950.00", "INR"),
            stock("SUNPHARMA", "Sun Pharmaceutical", "INDIA", "NSE", "1520.00", "INR"),
            stock("RELIANCE-BSE", "Reliance Industries", "INDIA", "BSE", "2860.00", "INR"),
            stock("TCS-BSE", "Tata Consultancy Services", "INDIA", "BSE", "3800.00", "INR"),
            stock("INFY-BSE", "Infosys", "INDIA", "BSE", "1500.00", "INR"),
            stock("HDFCBANK-BSE", "HDFC Bank", "INDIA", "BSE", "1680.00", "INR"),
            stock("BAJFINANCE-BSE", "Bajaj Finance", "INDIA", "BSE", "7200.00", "INR"),
            stock("AAPL", "Apple", "US", "NASDAQ", "210.00", "USD"),
            stock("MSFT", "Microsoft", "US", "NASDAQ", "450.00", "USD"),
            stock("GOOGL", "Alphabet", "US", "NASDAQ", "175.00", "USD"),
            stock("AMZN", "Amazon", "US", "NASDAQ", "185.00", "USD"),
            stock("NVDA", "NVIDIA", "US", "NASDAQ", "125.00", "USD"),
            stock("META", "Meta Platforms", "US", "NASDAQ", "500.00", "USD"),
            stock("TSLA", "Tesla", "US", "NASDAQ", "250.00", "USD"),
            stock("JPM", "JPMorgan Chase", "US", "NYSE", "205.00", "USD"),
            stock("V", "Visa", "US", "NYSE", "275.00", "USD"),
            stock("WMT", "Walmart", "US", "NYSE", "70.00", "USD")
    );

    public List<InstrumentResponse> search(String market, String query) {
        String marketFilter = normalize(market);
        String queryFilter = normalize(query);

        return instruments.stream()
                .filter(instrument -> marketFilter.isBlank()
                        || instrument.getMarket().equalsIgnoreCase(marketFilter)
                        || instrument.getExchange().equalsIgnoreCase(marketFilter))
                .filter(instrument -> queryFilter.isBlank()
                        || normalize(instrument.getSymbol()).contains(queryFilter)
                        || normalize(instrument.getName()).contains(queryFilter)
                        || normalize(instrument.getExchange()).contains(queryFilter))
                .toList();
    }

    public boolean exists(String symbol) {
        String normalizedSymbol = normalize(symbol);
        return instruments.stream()
                .anyMatch(instrument -> normalize(instrument.getSymbol()).equals(normalizedSymbol));
    }

    public InstrumentResponse findBySymbol(String symbol) {
        String normalizedSymbol = normalize(symbol);
        return instruments.stream()
                .filter(instrument -> normalize(instrument.getSymbol()).equals(normalizedSymbol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("symbol is not available in the demo market list"));
    }

    private static InstrumentResponse stock(
            String symbol,
            String name,
            String market,
            String exchange,
            String referencePrice,
            String currency) {
        return new InstrumentResponse(symbol, name, market, exchange, new BigDecimal(referencePrice), currency);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
