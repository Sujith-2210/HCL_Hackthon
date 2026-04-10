package com.hclhackathon.hotel.service;

import com.hclhackathon.hotel.domain.HotelEntity;
import com.hclhackathon.hotel.domain.RoomTypeEntity;
import com.hclhackathon.hotel.repo.AmenityRepository;
import com.hclhackathon.hotel.repo.BookingRepository;
import com.hclhackathon.hotel.repo.HotelAmenityRepository;
import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
	private final RoomTypeRepository roomTypes;
	private final RoomInventoryRepository roomInventory;
	private final BookingRepository bookings;
	private final HotelAmenityRepository hotelAmenities;
	private final AmenityRepository amenities;

	public RoomService(
		RoomTypeRepository roomTypes,
		RoomInventoryRepository roomInventory,
		BookingRepository bookings,
		HotelAmenityRepository hotelAmenities,
		AmenityRepository amenities
	) {
		this.roomTypes = roomTypes;
		this.roomInventory = roomInventory;
		this.bookings = bookings;
		this.hotelAmenities = hotelAmenities;
		this.amenities = amenities;
	}

	public HotelService.HotelSearchResult toHotelSearchResult(
		HotelEntity hotel,
		LocalDate checkIn,
		LocalDate checkOut,
		Integer guestCount
	) {
		var roomTypeEntities = roomTypes.findByHotelId(hotel.id);
		var roomAvailability = roomTypeEntities.stream()
			.map(rt -> toRoomAvailability(rt, checkIn, checkOut, guestCount))
			.toList();

		return new HotelService.HotelSearchResult(
			hotel.id,
			hotel.name,
			hotel.city,
			hotel.country,
			hotel.starRating,
			roomAvailability
		);
	}

	public HotelService.HotelDetails toHotelDetails(
		HotelEntity hotel,
		LocalDate checkIn,
		LocalDate checkOut,
		Integer guestCount
	) {
		var amenityIds = hotelAmenities.findAmenityIdsByHotelId(hotel.id);
		var amenityNames = amenityIds.isEmpty()
			? List.<String>of()
			: amenities.findByIdIn(amenityIds).stream().map(a -> a.name).sorted().toList();

		var roomTypeEntities = roomTypes.findByHotelId(hotel.id);
		var roomAvailability = roomTypeEntities.stream()
			.map(rt -> toRoomAvailability(rt, checkIn, checkOut, guestCount))
			.toList();

		return new HotelService.HotelDetails(
			hotel.id,
			hotel.name,
			hotel.description,
			hotel.address,
			hotel.city,
			hotel.country,
			hotel.starRating,
			amenityNames,
			roomAvailability
		);
	}

	public List<String> getAmenityNamesByHotelId(UUID hotelId) {
		var amenityIds = hotelAmenities.findAmenityIdsByHotelId(hotelId);
		if (amenityIds.isEmpty()) return List.of();
		return amenities.findByIdIn(amenityIds).stream().map(a -> a.name).sorted().toList();
	}

	private HotelService.RoomAvailability toRoomAvailability(
		RoomTypeEntity roomType,
		LocalDate checkIn,
		LocalDate checkOut,
		Integer guestCount
	) {
		if (guestCount != null && guestCount > roomType.maxOccupancy)
			return new HotelService.RoomAvailability(roomType.id, roomType.name, roomType.description, roomType.maxOccupancy, roomType.currency, roomType.basePricePerNight.toPlainString(), 0);

		if (checkIn == null || checkOut == null)
			return new HotelService.RoomAvailability(roomType.id, roomType.name, roomType.description, roomType.maxOccupancy, roomType.currency, roomType.basePricePerNight.toPlainString(), countTotalActiveRooms(roomType.id));

		var roomIds = roomInventory.findByRoomTypeIdAndIsActiveTrue(roomType.id).stream().map(r -> r.id).toList();
		if (roomIds.isEmpty())
			return new HotelService.RoomAvailability(roomType.id, roomType.name, roomType.description, roomType.maxOccupancy, roomType.currency, roomType.basePricePerNight.toPlainString(), 0);

		var overlapping = bookings.countActiveOverlappingBookings(roomIds, checkIn, checkOut);
		var available = Math.max(0, roomIds.size() - overlapping);
		return new HotelService.RoomAvailability(roomType.id, roomType.name, roomType.description, roomType.maxOccupancy, roomType.currency, roomType.basePricePerNight.toPlainString(), available);
	}

	private long countTotalActiveRooms(UUID roomTypeId) {
		return roomInventory.findByRoomTypeIdAndIsActiveTrue(roomTypeId).size();
	}
}
