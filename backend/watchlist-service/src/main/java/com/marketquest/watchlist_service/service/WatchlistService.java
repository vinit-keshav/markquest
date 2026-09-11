package com.marketquest.watchlist_service.service;

import com.marketquest.watchlist_service.dto.WatchlistRequest;
import com.marketquest.watchlist_service.entity.WatchlistItem;  
import com.marketquest.watchlist_service.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public List<WatchlistItem> getWatchlist(String userId){
        return watchlistRepository.findByUserIdOrderBySymbol(userId);
    }

    public WatchlistItem postWatchlist(String userId, WatchlistRequest request) {
        WatchlistItem item = new WatchlistItem();
        item.setUserId(userId);
        if (request.getSymbol() == null || !request.getSymbol().matches("[A-Za-z0-9.-]{1,30}")) throw new IllegalArgumentException("Enter a valid stock symbol");
        if (request.getCompanyName() == null || request.getCompanyName().isBlank() || request.getCompanyName().length() > 200) throw new IllegalArgumentException("Company name is required (max 200 characters)");
        item.setSymbol(request.getSymbol().toUpperCase(java.util.Locale.ROOT));
        item.setCompanyName(request.getCompanyName());
        return watchlistRepository.save(item);
    }

    public WatchlistItem updateWatchlist(String userId, Long id, WatchlistRequest request) {
        WatchlistItem item = watchlistRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Stock not found"));
        if (request.getNote() != null && request.getNote().length() > 255) throw new IllegalArgumentException("Note must be at most 255 characters");
        item.setNote(request.getNote());
        return watchlistRepository.save(item);
    }

}