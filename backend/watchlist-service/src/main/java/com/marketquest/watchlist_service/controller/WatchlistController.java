package com.marketquest.watchlist_service.controller;

import com.marketquest.watchlist_service.dto.WatchlistRequest;
import com.marketquest.watchlist_service.entity.WatchlistItem;
import com.marketquest.watchlist_service.service.WatchlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping({"", "/saveWatchlist"})
    public WatchlistItem watchlistPost(@RequestBody WatchlistRequest request, java.security.Principal principal) {
        return watchlistService.postWatchlist(principal.getName(), request);
    }

    @GetMapping({"", "/getWatchlist"})
    public List<WatchlistItem> watchListGet(java.security.Principal principal) {
        return watchlistService.getWatchlist(principal.getName());
    }

    @PutMapping({"/{id}", "/updateWatchlist/{id}"})
    public WatchlistItem watchlistPut(@PathVariable Long id, @RequestBody WatchlistRequest request, java.security.Principal principal) {
        return watchlistService.updateWatchlist(principal.getName(), id, request);
    }

}
