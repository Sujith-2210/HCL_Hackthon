package com.hotel.dto.response;

import java.math.BigDecimal;

public class HotelResponse {
    private Long id;
    private String name, location, city, country, description, imageUrl, amenities, category;
    private Double rating;
    private BigDecimal pricePerNight;
    private int availableRooms;

    public HotelResponse() {}
    private HotelResponse(Builder b) {
        this.id = b.id; this.name = b.name; this.location = b.location;
        this.city = b.city; this.country = b.country; this.description = b.description;
        this.imageUrl = b.imageUrl; this.rating = b.rating; this.pricePerNight = b.pricePerNight;
        this.amenities = b.amenities; this.category = b.category; this.availableRooms = b.availableRooms;
    }
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long id; private String name, location, city, country, description, imageUrl, amenities, category;
        private Double rating; private BigDecimal pricePerNight; private int availableRooms;
        public Builder id(Long v) { this.id = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder location(String v) { this.location = v; return this; }
        public Builder city(String v) { this.city = v; return this; }
        public Builder country(String v) { this.country = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder imageUrl(String v) { this.imageUrl = v; return this; }
        public Builder rating(Double v) { this.rating = v; return this; }
        public Builder pricePerNight(BigDecimal v) { this.pricePerNight = v; return this; }
        public Builder amenities(String v) { this.amenities = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder availableRooms(int v) { this.availableRooms = v; return this; }
        public HotelResponse build() { return new HotelResponse(this); }
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Double getRating() { return rating; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public String getAmenities() { return amenities; }
    public String getCategory() { return category; }
    public int getAvailableRooms() { return availableRooms; }
}
