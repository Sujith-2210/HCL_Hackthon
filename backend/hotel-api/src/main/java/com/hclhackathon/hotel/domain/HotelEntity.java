package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hotels")
public class HotelEntity {
	@Id
	public UUID id;

	@Column(nullable = false)
	public String name;

	@Column(nullable = false)
	public String description;

	@Column(nullable = false)
	public String address;

	@Column(nullable = false)
	public String city;

	@Column(nullable = false)
	public String country;

	@Column(name = "star_rating", nullable = false)
	public Integer starRating;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;
}

