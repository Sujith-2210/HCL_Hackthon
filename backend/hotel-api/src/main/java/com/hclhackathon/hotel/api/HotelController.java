package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.service.HotelService;
import com.hclhackathon.hotel.service.SerpApiHotelService;
import com.hclhackathon.hotel.domain.HotelEntity;
import com.hclhackathon.hotel.domain.RoomInventoryEntity;
import com.hclhackathon.hotel.domain.RoomTypeEntity;
import com.hclhackathon.hotel.repo.HotelRepository;
import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
	private final HotelService hotels;
	private final SerpApiHotelService serpHotels;
	private final HotelRepository hotelRepository;
	private final RoomTypeRepository roomTypeRepository;
	private final RoomInventoryRepository roomInventoryRepository;

	public HotelController(
		HotelService hotels,
		SerpApiHotelService serpHotels,
		HotelRepository hotelRepository,
		RoomTypeRepository roomTypeRepository,
		RoomInventoryRepository roomInventoryRepository
	) {
		this.hotels = hotels;
		this.serpHotels = serpHotels;
		this.hotelRepository = hotelRepository;
		this.roomTypeRepository = roomTypeRepository;
		this.roomInventoryRepository = roomInventoryRepository;
	}

	@GetMapping
	public ApiResponse<List<HotelCardResponse>> search(
		@RequestParam(required = false) String city,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
		@RequestParam(required = false) @Min(1) Integer guests,
		@RequestParam(required = false) String amenities,
		@RequestParam(required = false) String category
	) {
		var hasCitySearch = city != null && !city.isBlank();
		if (serpHotels.isEnabled() && hasCitySearch) {
			var query = city.trim();
			try {
				var liveHotels = serpHotels.search(query, checkIn, checkOut, guests)
					.stream()
					.map(this::toHotelCardResponse)
					.toList();
				if (!liveHotels.isEmpty()) return ApiResponse.of(liveHotels);
			} catch (Exception ignored) {
				// Fall back to local DB results if live provider is unavailable.
			}
		}

		var amenityFilters = amenities == null ? List.<String>of() :
			Arrays.stream(amenities.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();

		var results = hotels.searchHotels(new HotelService.HotelSearchRequest(city, checkIn, checkOut, guests, amenityFilters));
		return ApiResponse.of(results.stream().map(this::toHotelCardResponse).toList());
	}

	@GetMapping("/{hotelId}")
	public ApiResponse<HotelDetailsResponse> getDetails(
		@PathVariable String hotelId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
		@RequestParam(required = false) @Min(1) Integer guests
	) {
		if (hotelId.startsWith("serp::")) {
			var hotel = serpHotels.getById(hotelId, checkIn, checkOut, guests);
			return ApiResponse.of(new HotelDetailsResponse(
				hotel.id(),
				hotel.name(),
				hotel.description(),
				"",
				hotel.city(),
				hotel.country(),
				hotel.location(),
				hotel.rating(),
				hotel.amenities(),
				hotel.imageUrl(),
				hotel.category()
			));
		}

		return ApiResponse.of(toHotelDetailsResponse(hotels.getHotelDetails(UUID.fromString(hotelId), checkIn, checkOut, guests)));
	}

	@GetMapping("/{hotelId}/rooms")
	public ApiResponse<List<RoomResponse>> rooms(
		@PathVariable String hotelId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
		@RequestParam(required = false) @Min(1) Integer guests
	) {
		if (hotelId.startsWith("serp::")) {
			var hotel = serpHotels.getById(hotelId, checkIn, checkOut, guests);
			var roomTypeId = ensureLocalInventoryForSerpHotel(hotel);
			var room = new RoomResponse(
				roomTypeId.toString(),
				"Standard",
				"1",
				hotel.description(),
				guests == null || guests < 1 ? 2 : guests,
				hotel.pricePerNight(),
				hotel.amenities(),
				hotel.imageUrl()
			);
			return ApiResponse.of(List.of(room));
		}

		var details = hotels.getHotelDetails(UUID.fromString(hotelId), checkIn, checkOut, guests);
		var rooms = details.roomTypes().stream()
			.filter(room -> room.availableCount() > 0)
			.map(room -> new RoomResponse(
				room.roomTypeId().toString(),
				room.name(),
				room.name(),
				room.description(),
				room.maxOccupancy(),
				Double.parseDouble(room.pricePerNight()),
				String.join(", ", details.amenities()),
				null
			))
			.toList();
		return ApiResponse.of(rooms);
	}

	private HotelCardResponse toHotelCardResponse(SerpApiHotelService.SerpHotel hotel) {
		return new HotelCardResponse(
			hotel.id(),
			hotel.name(),
			hotel.city(),
			hotel.country(),
			hotel.rating(),
			hotel.amenities(),
			hotel.pricePerNight(),
			hotel.availableRooms(),
			hotel.imageUrl(),
			hotel.category()
		);
	}

	private HotelCardResponse toHotelCardResponse(HotelService.HotelSearchResult hotel) {
		var totalAvailable = hotel.roomTypes().stream().mapToLong(HotelService.RoomAvailability::availableCount).sum();
		var cheapest = hotel.roomTypes().stream()
			.mapToDouble(room -> Double.parseDouble(room.pricePerNight()))
			.min()
			.orElse(0);
		var details = hotels.getHotelDetails(hotel.hotelId(), null, null, null);
		return new HotelCardResponse(
			hotel.hotelId().toString(),
			hotel.name(),
			hotel.city(),
			hotel.country(),
			hotel.starRating() == null ? 0d : hotel.starRating().doubleValue(),
			String.join(", ", details.amenities()),
			cheapest,
			totalAvailable,
			null,
			"Hotel"
		);
	}

	private HotelDetailsResponse toHotelDetailsResponse(HotelService.HotelDetails hotel) {
		return new HotelDetailsResponse(
			hotel.hotelId().toString(),
			hotel.name(),
			hotel.description(),
			hotel.address(),
			hotel.city(),
			hotel.country(),
			hotel.city() + ", " + hotel.country(),
			hotel.starRating() == null ? 0d : hotel.starRating().doubleValue(),
			String.join(", ", hotel.amenities()),
			null,
			"Hotel"
		);
	}

	public record HotelCardResponse(
		String id,
		String name,
		String city,
		String country,
		double rating,
		String amenities,
		double pricePerNight,
		long availableRooms,
		String imageUrl,
		String category
	) {}

	public record HotelDetailsResponse(
		String id,
		String name,
		String description,
		String address,
		String city,
		String country,
		String location,
		double rating,
		String amenities,
		String imageUrl,
		String category
	) {}

	public record RoomResponse(
		String id,
		String roomType,
		String roomNumber,
		String description,
		int capacity,
		double pricePerNight,
		String amenities,
		String imageUrl
	) {}

	private UUID ensureLocalInventoryForSerpHotel(SerpApiHotelService.SerpHotel serpHotel) {
		var hotelId = stableUuid("serp-hotel:" + serpHotel.propertyToken());
		var roomTypeId = stableUuid("serp-room-type:" + serpHotel.propertyToken());
		var roomInventoryId = stableUuid("serp-room-inventory:" + serpHotel.propertyToken());

		var existingHotel = hotelRepository.findById(hotelId).orElse(null);
		if (existingHotel == null) {
			var hotel = new HotelEntity();
			hotel.id = hotelId;
			hotel.name = serpHotel.name();
			hotel.description = serpHotel.description() == null || serpHotel.description().isBlank() ? "Live hotel from external provider" : serpHotel.description();
			hotel.address = serpHotel.location() == null || serpHotel.location().isBlank() ? (serpHotel.city() + ", " + serpHotel.country()) : serpHotel.location();
			hotel.city = serpHotel.city();
			hotel.country = serpHotel.country();
			hotel.starRating = (int) Math.max(3, Math.min(5, Math.round(serpHotel.rating())));
			hotel.createdAt = Instant.now();
			hotelRepository.save(hotel);
		}

		var existingRoomType = roomTypeRepository.findById(roomTypeId).orElse(null);
		if (existingRoomType == null) {
			var roomType = new RoomTypeEntity();
			roomType.id = roomTypeId;
			roomType.hotelId = hotelId;
			roomType.name = "Standard";
			roomType.description = serpHotel.description() == null || serpHotel.description().isBlank() ? "Live provider room" : serpHotel.description();
			roomType.maxOccupancy = 2;
			roomType.basePricePerNight = BigDecimal.valueOf(Math.max(serpHotel.pricePerNight(), 1d));
			roomType.currency = "INR";
			roomTypeRepository.save(roomType);
		}

		var existingInventory = roomInventoryRepository.findById(roomInventoryId).orElse(null);
		if (existingInventory == null) {
			var inventory = new RoomInventoryEntity();
			inventory.id = roomInventoryId;
			inventory.roomTypeId = roomTypeId;
			inventory.roomLabel = "LIVE-" + roomInventoryId.toString().substring(0, 8).toUpperCase();
			inventory.isActive = true;
			roomInventoryRepository.save(inventory);
		}

		return roomTypeId;
	}

	private static UUID stableUuid(String source) {
		return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
	}
}
