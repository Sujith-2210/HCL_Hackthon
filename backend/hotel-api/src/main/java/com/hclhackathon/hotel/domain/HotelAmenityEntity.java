package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "hotel_amenities")
public class HotelAmenityEntity {
	@EmbeddedId
	public HotelAmenityId id;

	@Embeddable
	public static class HotelAmenityId implements Serializable {
		@Column(name = "hotel_id", nullable = false)
		public UUID hotelId;

		@Column(name = "amenity_id", nullable = false)
		public UUID amenityId;

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof HotelAmenityId that)) return false;
			return Objects.equals(hotelId, that.hotelId) && Objects.equals(amenityId, that.amenityId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(hotelId, amenityId);
		}
	}
}

