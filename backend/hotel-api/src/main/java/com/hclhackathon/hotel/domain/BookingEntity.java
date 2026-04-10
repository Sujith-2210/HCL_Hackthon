package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class BookingEntity {
	@Id
	public UUID id;

	@Column(name = "user_id", nullable = false)
	public UUID userId;

	@Column(name = "room_inventory_id", nullable = false)
	public UUID roomInventoryId;

	@Column(name = "check_in", nullable = false)
	public LocalDate checkIn;

	@Column(name = "check_out", nullable = false)
	public LocalDate checkOut;

	@Column(name = "guest_count", nullable = false)
	public Integer guestCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public BookingStatus status;

	@Column(name = "total_amount", nullable = false)
	public BigDecimal totalAmount;

	@Column(nullable = false)
	public String currency;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	public Instant updatedAt;
}

