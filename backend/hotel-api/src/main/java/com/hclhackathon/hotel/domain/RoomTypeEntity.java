package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "room_types")
public class RoomTypeEntity {
	@Id
	public UUID id;

	@Column(name = "hotel_id", nullable = false)
	public UUID hotelId;

	@Column(nullable = false)
	public String name;

	@Column(nullable = false)
	public String description;

	@Column(name = "max_occupancy", nullable = false)
	public Integer maxOccupancy;

	@Column(name = "base_price_per_night", nullable = false)
	public BigDecimal basePricePerNight;

	@Column(nullable = false)
	public String currency;
}

