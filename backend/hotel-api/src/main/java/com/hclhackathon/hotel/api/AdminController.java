package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.domain.HotelEntity;
import com.hclhackathon.hotel.domain.Role;
import com.hclhackathon.hotel.domain.RoomTypeEntity;
import com.hclhackathon.hotel.repo.BookingRepository;
import com.hclhackathon.hotel.repo.HotelRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import com.hclhackathon.hotel.repo.UserRepository;
import com.hclhackathon.hotel.service.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final UserRepository users;
	private final HotelRepository hotels;
	private final RoomTypeRepository roomTypes;
	private final BookingRepository bookings;

	public AdminController(UserRepository users, HotelRepository hotels, RoomTypeRepository roomTypes, BookingRepository bookings) {
		this.users = users;
		this.hotels = hotels;
		this.roomTypes = roomTypes;
		this.bookings = bookings;
	}

	@GetMapping("/users")
	public ApiResponse<List<UserView>> users() {
		return ApiResponse.of(users.findAll().stream().map(u -> new UserView(u.id.toString(), u.fullName, u.email, u.role.name())).toList());
	}

	@PatchMapping("/users/{userId}/role")
	public ApiResponse<UserView> updateUserRole(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRoleRequest request) {
		var user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
		user.role = request.role();
		users.save(user);
		return ApiResponse.of(new UserView(user.id.toString(), user.fullName, user.email, user.role.name()));
	}

	@DeleteMapping("/users/{userId}")
	public ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
		if (!users.existsById(userId)) throw new NotFoundException("User not found");
		users.deleteById(userId);
		return ApiResponse.of(null);
	}

	@GetMapping("/hotels")
	public ApiResponse<List<HotelView>> hotels() {
		return ApiResponse.of(hotels.findAll().stream().map(h -> new HotelView(h.id.toString(), h.name, h.city, h.country, h.starRating)).toList());
	}

	@PostMapping("/hotels")
	public ApiResponse<HotelView> createHotel(@Valid @RequestBody CreateHotelRequest request) {
		var hotel = new HotelEntity();
		hotel.id = UUID.randomUUID();
		hotel.name = request.name().trim();
		hotel.description = request.description().trim();
		hotel.address = request.address().trim();
		hotel.city = request.city().trim();
		hotel.country = request.country().trim();
		hotel.starRating = request.starRating();
		hotel.createdAt = Instant.now();
		hotels.save(hotel);
		return ApiResponse.of(new HotelView(hotel.id.toString(), hotel.name, hotel.city, hotel.country, hotel.starRating));
	}

	@PatchMapping("/hotels/{hotelId}")
	public ApiResponse<HotelView> updateHotel(@PathVariable UUID hotelId, @Valid @RequestBody UpdateHotelRequest request) {
		var hotel = hotels.findById(hotelId).orElseThrow(() -> new NotFoundException("Hotel not found"));
		hotel.name = request.name().trim();
		hotel.description = request.description().trim();
		hotel.address = request.address().trim();
		hotel.city = request.city().trim();
		hotel.country = request.country().trim();
		hotel.starRating = request.starRating();
		hotels.save(hotel);
		return ApiResponse.of(new HotelView(hotel.id.toString(), hotel.name, hotel.city, hotel.country, hotel.starRating));
	}

	@DeleteMapping("/hotels/{hotelId}")
	public ApiResponse<Void> deleteHotel(@PathVariable UUID hotelId) {
		if (!hotels.existsById(hotelId)) throw new NotFoundException("Hotel not found");
		hotels.deleteById(hotelId);
		return ApiResponse.of(null);
	}

	@GetMapping("/rooms")
	public ApiResponse<List<RoomView>> rooms(@RequestParam(required = false) UUID hotelId) {
		var items = roomTypes.findAll();
		if (hotelId != null) items = items.stream().filter(r -> r.hotelId.equals(hotelId)).toList();
		return ApiResponse.of(items.stream().map(r -> new RoomView(r.id.toString(), r.hotelId.toString(), r.name, r.maxOccupancy, r.basePricePerNight.toPlainString(), r.currency)).toList());
	}

	@PostMapping("/rooms")
	public ApiResponse<RoomView> createRoom(@Valid @RequestBody CreateRoomRequest request) {
		var room = new RoomTypeEntity();
		room.id = UUID.randomUUID();
		room.hotelId = request.hotelId();
		room.name = request.name().trim();
		room.description = request.description().trim();
		room.maxOccupancy = request.maxOccupancy();
		room.basePricePerNight = request.basePricePerNight();
		room.currency = request.currency().trim().toUpperCase();
		roomTypes.save(room);
		return ApiResponse.of(new RoomView(room.id.toString(), room.hotelId.toString(), room.name, room.maxOccupancy, room.basePricePerNight.toPlainString(), room.currency));
	}

	@PatchMapping("/rooms/{roomId}")
	public ApiResponse<RoomView> updateRoom(@PathVariable UUID roomId, @Valid @RequestBody UpdateRoomRequest request) {
		var room = roomTypes.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
		room.hotelId = request.hotelId();
		room.name = request.name().trim();
		room.description = request.description().trim();
		room.maxOccupancy = request.maxOccupancy();
		room.basePricePerNight = request.basePricePerNight();
		room.currency = request.currency().trim().toUpperCase();
		roomTypes.save(room);
		return ApiResponse.of(new RoomView(room.id.toString(), room.hotelId.toString(), room.name, room.maxOccupancy, room.basePricePerNight.toPlainString(), room.currency));
	}

	@DeleteMapping("/rooms/{roomId}")
	public ApiResponse<Void> deleteRoom(@PathVariable UUID roomId) {
		if (!roomTypes.existsById(roomId)) throw new NotFoundException("Room not found");
		roomTypes.deleteById(roomId);
		return ApiResponse.of(null);
	}

	@GetMapping("/reports")
	public ApiResponse<ReportView> report() {
		var allUsers = users.count();
		var allHotels = hotels.count();
		var allRooms = roomTypes.count();
		var allBookings = bookings.count();
		return ApiResponse.of(new ReportView(allUsers, allHotels, allRooms, allBookings));
	}

	public record UserView(String id, String fullName, String email, String role) {}
	public record HotelView(String id, String name, String city, String country, Integer starRating) {}
	public record RoomView(String id, String hotelId, String name, Integer maxOccupancy, String basePricePerNight, String currency) {}
	public record ReportView(long users, long hotels, long rooms, long bookings) {}

	public record CreateHotelRequest(
		@NotBlank String name,
		@NotBlank String description,
		@NotBlank String address,
		@NotBlank String city,
		@NotBlank String country,
		@NotNull @Min(1) Integer starRating
	) {}

	public record CreateRoomRequest(
		@NotNull UUID hotelId,
		@NotBlank String name,
		@NotBlank String description,
		@NotNull @Min(1) Integer maxOccupancy,
		@NotNull BigDecimal basePricePerNight,
		@NotBlank String currency
	) {}

	public record UpdateHotelRequest(
		@NotBlank String name,
		@NotBlank String description,
		@NotBlank String address,
		@NotBlank String city,
		@NotBlank String country,
		@NotNull @Min(1) Integer starRating
	) {}

	public record UpdateRoomRequest(
		@NotNull UUID hotelId,
		@NotBlank String name,
		@NotBlank String description,
		@NotNull @Min(1) Integer maxOccupancy,
		@NotNull BigDecimal basePricePerNight,
		@NotBlank String currency
	) {}

	public record UpdateUserRoleRequest(
		@NotNull Role role
	) {}
}

