package com.hotel.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt;

    public Booking() {}

    private Booking(Builder b) {
        this.bookingReference = b.bookingReference;
        this.user = b.user;
        this.room = b.room;
        this.checkInDate = b.checkInDate;
        this.checkOutDate = b.checkOutDate;
        this.numberOfGuests = b.numberOfGuests;
        this.totalPrice = b.totalPrice;
        this.status = b.status != null ? b.status : BookingStatus.PENDING;
        this.specialRequests = b.specialRequests;
        this.createdAt = LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String bookingReference;
        private User user;
        private Room room;
        private LocalDate checkInDate, checkOutDate;
        private Integer numberOfGuests;
        private BigDecimal totalPrice;
        private BookingStatus status;
        private String specialRequests;

        public Builder bookingReference(String v) { this.bookingReference = v; return this; }
        public Builder user(User v) { this.user = v; return this; }
        public Builder room(Room v) { this.room = v; return this; }
        public Builder checkInDate(LocalDate v) { this.checkInDate = v; return this; }
        public Builder checkOutDate(LocalDate v) { this.checkOutDate = v; return this; }
        public Builder numberOfGuests(Integer v) { this.numberOfGuests = v; return this; }
        public Builder totalPrice(BigDecimal v) { this.totalPrice = v; return this; }
        public Builder status(BookingStatus v) { this.status = v; return this; }
        public Builder specialRequests(String v) { this.specialRequests = v; return this; }
        public Booking build() { return new Booking(this); }
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String v) { this.bookingReference = v; }
    public User getUser() { return user; }
    public void setUser(User v) { this.user = v; }
    public Room getRoom() { return room; }
    public void setRoom(Room v) { this.room = v; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate v) { this.checkInDate = v; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate v) { this.checkOutDate = v; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer v) { this.numberOfGuests = v; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal v) { this.totalPrice = v; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus v) { this.status = v; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String v) { this.specialRequests = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
