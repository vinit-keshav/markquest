package com.marketquest.watchlist_service.controller;

import com.marketquest.watchlist_service.dto.WatchlistRequest;
import com.marketquest.watchlist_service.entity.WatchlistItem;
import com.marketquest.watchlist_service.service.WatchlistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "*")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping({"", "/saveWatchlist"})
    public WatchlistItem watchlistPost(@RequestBody WatchlistRequest request) {
        return watchlistService.postWatchlist(request);
    }

    @GetMapping({"", "/getWatchlist"})
    public List<WatchlistItem> watchListGet() {
        return watchlistService.getWatchlist();
    }

    @PutMapping({"/{id}", "/updateWatchlist/{id}"})
    public WatchlistItem watchlistPut(@PathVariable Long id, @RequestBody WatchlistRequest request) {
        return watchlistService.updateWatchlist(id, request);
    }

}
