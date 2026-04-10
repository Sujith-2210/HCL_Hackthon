package com.hotel.dto.response;

import java.math.BigDecimal;

public class RoomResponse {
    private Long id, hotelId;
    private String hotelName, roomNumber, roomType, description, amenities, imageUrl;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private boolean available;

    public RoomResponse() {}
    private RoomResponse(Builder b) {
        this.id = b.id; this.hotelId = b.hotelId; this.hotelName = b.hotelName;
        this.roomNumber = b.roomNumber; this.roomType = b.roomType; this.capacity = b.capacity;
        this.pricePerNight = b.pricePerNight; this.description = b.description;
        this.amenities = b.amenities; this.imageUrl = b.imageUrl; this.available = b.available;
    }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id, hotelId; private String hotelName, roomNumber, roomType, description, amenities, imageUrl;
        private Integer capacity; private BigDecimal pricePerNight; private boolean available;
        public Builder id(Long v) { this.id = v; return this; }
        public Builder hotelId(Long v) { this.hotelId = v; return this; }
        public Builder hotelName(String v) { this.hotelName = v; return this; }
        public Builder roomNumber(String v) { this.roomNumber = v; return this; }
        public Builder roomType(String v) { this.roomType = v; return this; }
        public Builder capacity(Integer v) { this.capacity = v; return this; }
        public Builder pricePerNight(BigDecimal v) { this.pricePerNight = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder amenities(String v) { this.amenities = v; return this; }
        public Builder imageUrl(String v) { this.imageUrl = v; return this; }
        public Builder available(boolean v) { this.available = v; return this; }
        public RoomResponse build() { return new RoomResponse(this); }
    }
    public Long getId() { return id; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public Integer getCapacity() { return capacity; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public String getDescription() { return description; }
    public String getAmenities() { return amenities; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return available; }
}
