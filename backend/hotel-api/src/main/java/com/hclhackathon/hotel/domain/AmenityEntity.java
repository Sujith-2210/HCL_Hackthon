package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "amenities")
public class AmenityEntity {
	@Id
	public UUID id;

	@Column(nullable = false, unique = true)
	public String name;
}

