package com.hotel.dto.response;

import java.util.List;

/**
 * DTO representing a hotel from SerpAPI Google Hotels search results.
 */
public class SerpApiHotelResponse {

    private String name;
    private String description;
    private String type;
    private double latitude;
    private double longitude;
    private String checkInTime;
    private String checkOutTime;
    private double overallRating;
    private int reviews;
    private String hotelClass;
    private int extractedHotelClass;
    private double pricePerNight;
    private double totalPrice;
    private String currency;
    private String thumbnail;
    private List<String> images;
    private List<String> amenities;
    private List<NearbyPlace> nearbyPlaces;
    private String propertyToken;
    private String link;

    public SerpApiHotelResponse() {}

    // --- Nested class for nearby places ---
    public static class NearbyPlace {
        private String name;
        private String transportType;
        private String duration;

        public NearbyPlace() {}

        public NearbyPlace(String name, String transportType, String duration) {
            this.name = name;
            this.transportType = transportType;
            this.duration = duration;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTransportType() { return transportType; }
        public void setTransportType(String transportType) { this.transportType = transportType; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    // --- Builder ---
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SerpApiHotelResponse r = new SerpApiHotelResponse();

        public Builder name(String v) { r.name = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder type(String v) { r.type = v; return this; }
        public Builder latitude(double v) { r.latitude = v; return this; }
        public Builder longitude(double v) { r.longitude = v; return this; }
        public Builder checkInTime(String v) { r.checkInTime = v; return this; }
        public Builder checkOutTime(String v) { r.checkOutTime = v; return this; }
        public Builder overallRating(double v) { r.overallRating = v; return this; }
        public Builder reviews(int v) { r.reviews = v; return this; }
        public Builder hotelClass(String v) { r.hotelClass = v; return this; }
        public Builder extractedHotelClass(int v) { r.extractedHotelClass = v; return this; }
        public Builder pricePerNight(double v) { r.pricePerNight = v; return this; }
        public Builder totalPrice(double v) { r.totalPrice = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder thumbnail(String v) { r.thumbnail = v; return this; }
        public Builder images(List<String> v) { r.images = v; return this; }
        public Builder amenities(List<String> v) { r.amenities = v; return this; }
        public Builder nearbyPlaces(List<NearbyPlace> v) { r.nearbyPlaces = v; return this; }
        public Builder propertyToken(String v) { r.propertyToken = v; return this; }
        public Builder link(String v) { r.link = v; return this; }
        public SerpApiHotelResponse build() { return r; }
    }

    // --- Getters & Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
    public double getOverallRating() { return overallRating; }
    public void setOverallRating(double overallRating) { this.overallRating = overallRating; }
    public int getReviews() { return reviews; }
    public void setReviews(int reviews) { this.reviews = reviews; }
    public String getHotelClass() { return hotelClass; }
    public void setHotelClass(String hotelClass) { this.hotelClass = hotelClass; }
    public int getExtractedHotelClass() { return extractedHotelClass; }
    public void setExtractedHotelClass(int extractedHotelClass) { this.extractedHotelClass = extractedHotelClass; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
    public List<NearbyPlace> getNearbyPlaces() { return nearbyPlaces; }
    public void setNearbyPlaces(List<NearbyPlace> nearbyPlaces) { this.nearbyPlaces = nearbyPlaces; }
    public String getPropertyToken() { return propertyToken; }
    public void setPropertyToken(String propertyToken) { this.propertyToken = propertyToken; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
