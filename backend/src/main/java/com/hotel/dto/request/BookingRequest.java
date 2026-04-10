package com.hotel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BookingRequest {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "At least 1 guest required")
    private Integer numberOfGuests;

    private String specialRequests;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long v) { this.roomId = v; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate v) { this.checkInDate = v; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate v) { this.checkOutDate = v; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer v) { this.numberOfGuests = v; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String v) { this.specialRequests = v; }
}
