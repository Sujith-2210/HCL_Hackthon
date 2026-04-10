package com.hotel.controller;

import com.hotel.dto.response.ApiResponse;
import com.hotel.dto.response.SerpApiHotelResponse;
import com.hotel.service.SerpApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for SerpAPI-powered hotel search.
 * Provides real-time hotel data from Google Hotels via SerpAPI.
 */
@RestController
@RequestMapping("/api/serpapi")
public class SerpApiController {
    private static final Logger log = LoggerFactory.getLogger(SerpApiController.class);
    private final SerpApiService serpApiService;

    public SerpApiController(SerpApiService serpApiService) {
        this.serpApiService = serpApiService;
    }

    /**
     * Search hotels using SerpAPI Google Hotels engine.
     * Example: GET /api/serpapi/hotels?q=hotels in Mumbai&checkIn=2026-04-15&checkOut=2026-04-17&adults=2&currency=INR
     */
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<SerpApiHotelResponse>>> searchHotels(
            @RequestParam String q,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(defaultValue = "2") int adults,
            @RequestParam(defaultValue = "INR") String currency) {
        log.info("GET /api/serpapi/hotels q={}, checkIn={}, checkOut={}, adults={}, currency={}",
                q, checkIn, checkOut, adults, currency);
        List<SerpApiHotelResponse> results = serpApiService.searchHotels(q, checkIn, checkOut, adults, currency);
        return ResponseEntity.ok(ApiResponse.success(
                results.size() + " hotels found via Google Hotels", results));
    }

    /**
     * Get detailed info for a specific hotel property.
     * Example: GET /api/serpapi/hotels/details?propertyToken=xxx&q=hotels in Mumbai&checkIn=...&checkOut=...
     */
    @GetMapping("/hotels/details")
    public ResponseEntity<ApiResponse<SerpApiHotelResponse>> getHotelDetails(
            @RequestParam String propertyToken,
            @RequestParam String q,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(defaultValue = "2") int adults,
            @RequestParam(defaultValue = "INR") String currency) {
        log.info("GET /api/serpapi/hotels/details token={}", propertyToken);
        SerpApiHotelResponse result = serpApiService.getHotelDetails(
                propertyToken, q, checkIn, checkOut, adults, currency);
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.success("Hotel details not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Hotel details loaded", result));
    }
}
