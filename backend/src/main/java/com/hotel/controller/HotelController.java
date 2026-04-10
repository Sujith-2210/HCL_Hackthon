package com.hotel.controller;

import com.hotel.dto.response.ApiResponse;
import com.hotel.dto.response.HotelResponse;
import com.hotel.dto.response.RoomResponse;
import com.hotel.service.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private static final Logger log = LoggerFactory.getLogger(HotelController.class);
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) { this.hotelService = hotelService; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelResponse>>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(required = false) String category) {
        log.info("GET /api/hotels city={}", city);
        return ResponseEntity.ok(ApiResponse.success(hotelService.searchHotels(city, checkIn, checkOut, guests, category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotel(@PathVariable Long id) {
        log.info("GET /api/hotels/{}", id);
        return ResponseEntity.ok(ApiResponse.success(hotelService.getHotelById(id)));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests) {
        log.info("GET /api/hotels/{}/rooms", id);
        return ResponseEntity.ok(ApiResponse.success(hotelService.getAvailableRooms(id, checkIn, checkOut, guests)));
    }
}
