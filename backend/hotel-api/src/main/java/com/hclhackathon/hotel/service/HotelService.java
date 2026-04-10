package com.hclhackathon.hotel.service;

import com.hclhackathon.hotel.domain.HotelEntity;
import com.hclhackathon.hotel.repo.HotelRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HotelService {
	private final HotelRepository hotels;
	private final RoomService rooms;

	public HotelService(HotelRepository hotels, RoomService rooms) {
		this.hotels = hotels;
		this.rooms = rooms;
	}

	public List<HotelSearchResult> searchHotels(HotelSearchRequest request) {
		var results = (request.city() == null || request.city().isBlank())
			? hotels.findAll()
			: hotels.findByCityIgnoreCase(request.city().trim());
		return results.stream()
			.filter(hotel -> matchesAmenityFilter(hotel.id, request.amenities()))
			.map(hotel -> rooms.toHotelSearchResult(hotel, request.checkIn(), request.checkOut(), request.guestCount()))
			.filter(r -> r.hasAnyAvailability())
			.toList();
	}

	public HotelDetails getHotelDetails(UUID hotelId, LocalDate checkIn, LocalDate checkOut, Integer guestCount) {
		var hotel = hotels.findById(hotelId).orElseThrow(() -> new NotFoundException("Hotel not found"));
		return rooms.toHotelDetails(hotel, checkIn, checkOut, guestCount);
	}

	private boolean matchesAmenityFilter(UUID hotelId, List<String> amenities) {
		if (amenities == null || amenities.isEmpty()) return true;
		var required = amenities.stream().map(String::trim).filter(v -> !v.isBlank()).map(String::toLowerCase).toList();
		if (required.isEmpty()) return true;

		var hotelAmenities = rooms.getAmenityNamesByHotelId(hotelId).stream()
			.map(String::toLowerCase)
			.toList();

		return required.stream().allMatch(hotelAmenities::contains);
	}

	public record HotelSearchRequest(String city, LocalDate checkIn, LocalDate checkOut, Integer guestCount, List<String> amenities) {}

	public record HotelSearchResult(
		UUID hotelId,
		String name,
		String city,
		String country,
		Integer starRating,
		List<RoomAvailability> roomTypes
	) {
		public boolean hasAnyAvailability() {
			return roomTypes.stream().anyMatch(rt -> rt.availableCount() > 0);
		}
	}

	public record HotelDetails(
		UUID hotelId,
		String name,
		String description,
		String address,
		String city,
		String country,
		Integer starRating,
		List<String> amenities,
		List<RoomAvailability> roomTypes
	) {}

	public record RoomAvailability(
		UUID roomTypeId,
		String name,
		String description,
		Integer maxOccupancy,
		String currency,
		String pricePerNight,
		long availableCount
	) {}
}

