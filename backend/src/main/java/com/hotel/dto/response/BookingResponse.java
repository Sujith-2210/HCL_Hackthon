package com.hotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id, userId, roomId, hotelId;
    private String bookingReference, userName, userEmail, roomNumber, roomType, hotelName, hotelCity, status, specialRequests;
    private LocalDate checkInDate, checkOutDate;
    private Integer numberOfNights, numberOfGuests;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    public BookingResponse() {}
    private BookingResponse(Builder b) {
        this.id = b.id; this.bookingReference = b.bookingReference; this.userId = b.userId;
        this.userName = b.userName; this.userEmail = b.userEmail; this.roomId = b.roomId;
        this.roomNumber = b.roomNumber; this.roomType = b.roomType; this.hotelId = b.hotelId;
        this.hotelName = b.hotelName; this.hotelCity = b.hotelCity; this.checkInDate = b.checkInDate;
        this.checkOutDate = b.checkOutDate; this.numberOfNights = b.numberOfNights;
        this.numberOfGuests = b.numberOfGuests; this.totalPrice = b.totalPrice;
        this.status = b.status; this.specialRequests = b.specialRequests; this.createdAt = b.createdAt;
    }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id, userId, roomId, hotelId;
        private String bookingReference, userName, userEmail, roomNumber, roomType, hotelName, hotelCity, status, specialRequests;
        private LocalDate checkInDate, checkOutDate;
        private Integer numberOfNights, numberOfGuests;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
        public Builder id(Long v) { this.id = v; return this; }
        public Builder bookingReference(String v) { this.bookingReference = v; return this; }
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder userName(String v) { this.userName = v; return this; }
        public Builder userEmail(String v) { this.userEmail = v; return this; }
        public Builder roomId(Long v) { this.roomId = v; return this; }
        public Builder roomNumber(String v) { this.roomNumber = v; return this; }
        public Builder roomType(String v) { this.roomType = v; return this; }
        public Builder hotelId(Long v) { this.hotelId = v; return this; }
        public Builder hotelName(String v) { this.hotelName = v; return this; }
        public Builder hotelCity(String v) { this.hotelCity = v; return this; }
        public Builder checkInDate(LocalDate v) { this.checkInDate = v; return this; }
        public Builder checkOutDate(LocalDate v) { this.checkOutDate = v; return this; }
        public Builder numberOfNights(Integer v) { this.numberOfNights = v; return this; }
        public Builder numberOfGuests(Integer v) { this.numberOfGuests = v; return this; }
        public Builder totalPrice(BigDecimal v) { this.totalPrice = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder specialRequests(String v) { this.specialRequests = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public BookingResponse build() { return new BookingResponse(this); }
    }
    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public Long getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public String getHotelCity() { return hotelCity; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public Integer getNumberOfNights() { return numberOfNights; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getSpecialRequests() { return specialRequests; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
