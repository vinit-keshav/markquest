package com.marketquest.trading.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketquest.trading.dto.InstrumentResponse;
import com.marketquest.trading.dto.LivePriceResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LivePriceService {
    private static final Duration CACHE_TTL = Duration.ofSeconds(20);

    private final String finnhubToken;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, LivePriceResponse> cache = new ConcurrentHashMap<>();

    public LivePriceService(@Value("${marketquest.market-data.finnhub-token:}") String finnhubToken) {
        this.finnhubToken = finnhubToken;
    }

    public LivePriceResponse getPrice(InstrumentResponse instrument) {
        String symbol = instrument.getSymbol().trim().toUpperCase(Locale.ROOT);
        LivePriceResponse cached = cache.get(symbol);
        if (cached != null && Duration.between(cached.getUpdatedAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cached;
        }

        LivePriceResponse response = fetchFinnhubPrice(instrument);
        cache.put(symbol, response);
        return response;
    }

    private LivePriceResponse fetchFinnhubPrice(InstrumentResponse instrument) {
        if (finnhubToken == null || finnhubToken.isBlank()) {
            return fallback(instrument);
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://finnhub.io/api/v1/quote")
                    .queryParam("symbol", providerSymbol(instrument))
                    .queryParam("token", finnhubToken)
                    .build()
                    .toUri();

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            BigDecimal currentPrice = json.path("c").decimalValue();

            if (response.statusCode() == 200 && currentPrice.signum() > 0) {
                return new LivePriceResponse(
                        instrument.getSymbol(),
                        currentPrice,
                        instrument.getCurrency(),
                        true,
                        Instant.now());
            }
        } catch (Exception ignored) {
        }

        return fallback(instrument);
    }

    private LivePriceResponse fallback(InstrumentResponse instrument) {
        return new LivePriceResponse(
                instrument.getSymbol(),
                instrument.getReferencePrice(),
                instrument.getCurrency(),
                false,
                Instant.now());
    }

    private String providerSymbol(InstrumentResponse instrument) {
        String symbol = instrument.getSymbol().replace("-BSE", "");
        if ("NSE".equalsIgnoreCase(instrument.getExchange())) {
            return "NSE:" + symbol;
        }
        if ("BSE".equalsIgnoreCase(instrument.getExchange())) {
            return "BSE:" + symbol;
        }
        return symbol;
    }
}
