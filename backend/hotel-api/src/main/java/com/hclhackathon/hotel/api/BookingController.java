package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.security.AuthUser;
import com.hclhackathon.hotel.domain.BookingStatus;
import com.hclhackathon.hotel.service.ConflictException;
import com.hclhackathon.hotel.service.NotFoundException;
import com.hclhackathon.hotel.service.BookingService;
import com.hclhackathon.hotel.repo.HotelRepository;
import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
	private final BookingService bookingService;
	private final RoomInventoryRepository roomInventory;
	private final RoomTypeRepository roomTypes;
	private final HotelRepository hotels;

	public BookingController(BookingService bookingService, RoomInventoryRepository roomInventory, RoomTypeRepository roomTypes, HotelRepository hotels) {
		this.bookingService = bookingService;
		this.roomInventory = roomInventory;
		this.roomTypes = roomTypes;
		this.hotels = hotels;
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	public ApiResponse<BookingResponse> create(Authentication auth, @Valid @RequestBody CreateBookingRequest request) {
		var principal = requireAuthUser(auth);
		var booking = bookingService.createBooking(principal.getUserId(), principal.getRole(), request.resolveRoomTypeId(), request.resolveCheckIn(), request.resolveCheckOut(), request.resolveGuestCount());
		return ApiResponse.of(toBookingResponse(booking));
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@GetMapping("/my")
	public ApiResponse<List<BookingResponse>> myBookings(Authentication auth) {
		var userId = requireUserId(auth);
		return ApiResponse.of(bookingService.getBookingHistory(userId).stream().map(this::toBookingResponse).toList());
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@GetMapping
	public ApiResponse<List<BookingResponse>> history(Authentication auth) {
		var userId = requireUserId(auth);
		return ApiResponse.of(bookingService.getBookingHistory(userId).stream().map(this::toBookingResponse).toList());
	}

	@PreAuthorize("hasRole('RECEPTIONIST') or hasRole('ADMIN')")
	@GetMapping("/all")
	public ApiResponse<List<BookingResponse>> allBookings() {
		return ApiResponse.of(bookingService.getAllBookings().stream().map(this::toBookingResponse).toList());
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@PatchMapping("/{bookingId}/cancel")
	public ApiResponse<BookingResponse> cancel(Authentication auth, @PathVariable UUID bookingId) {
		var userId = requireUserId(auth);
		return ApiResponse.of(toBookingResponse(bookingService.cancelBooking(userId, bookingId)));
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@org.springframework.web.bind.annotation.DeleteMapping("/{bookingId}")
	public ApiResponse<BookingResponse> deleteCancel(Authentication auth, @PathVariable UUID bookingId) {
		return cancel(auth, bookingId);
	}

	@PreAuthorize("hasRole('RECEPTIONIST')")
	@PatchMapping("/{bookingId}/complete")
	public ApiResponse<BookingResponse> complete(@PathVariable UUID bookingId) {
		return ApiResponse.of(toBookingResponse(bookingService.completeBooking(bookingId)));
	}

	@PreAuthorize("hasRole('RECEPTIONIST')")
	@PatchMapping("/{bookingId}/confirm")
	public ApiResponse<BookingResponse> confirm(@PathVariable UUID bookingId) {
		return ApiResponse.of(toBookingResponse(bookingService.confirmBooking(bookingId)));
	}

	@PreAuthorize("hasRole('RECEPTIONIST')")
	@PatchMapping("/{bookingId}/check-in")
	public ApiResponse<BookingResponse> checkIn(@PathVariable UUID bookingId) {
		return ApiResponse.of(toBookingResponse(bookingService.checkInBooking(bookingId)));
	}

	@PreAuthorize("hasRole('RECEPTIONIST')")
	@PatchMapping("/{bookingId}/check-out")
	public ApiResponse<BookingResponse> checkOut(@PathVariable UUID bookingId) {
		return complete(bookingId);
	}

	@PreAuthorize("hasRole('RECEPTIONIST')")
	@PatchMapping("/{bookingId}/status")
	public ApiResponse<BookingResponse> updateStatus(@PathVariable UUID bookingId, @Valid @RequestBody UpdateStatusRequest request) {
		return switch (request.status()) {
			case CONFIRMED -> confirm(bookingId);
			case CHECKED_IN -> checkIn(bookingId);
			case COMPLETED -> checkOut(bookingId);
			case CANCELLED -> ApiResponse.of(toBookingResponse(bookingService.cancelBookingByStaff(bookingId)));
			case PENDING -> throw new ConflictException("Cannot set booking back to pending");
		};
	}

	private static UUID requireUserId(Authentication auth) {
		return requireAuthUser(auth).getUserId();
	}

	private static AuthUser requireAuthUser(Authentication auth) {
		if (auth == null || auth.getPrincipal() == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		if (!(auth.getPrincipal() instanceof AuthUser au)) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		return au;
	}

	public record CreateBookingRequest(
		UUID roomTypeId,
		String roomId,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
		Integer guestCount,
		Integer numberOfGuests,
		String specialRequests
	) {
		public UUID resolveRoomTypeId() {
			if (roomTypeId != null) return roomTypeId;
			if (roomId != null && !roomId.isBlank()) return parseUuid(roomId, "roomId");
			throw new ConflictException("roomId is required");
		}

		public LocalDate resolveCheckIn() {
			if (checkIn != null) return checkIn;
			if (checkInDate != null) return checkInDate;
			throw new ConflictException("checkInDate is required");
		}

		public LocalDate resolveCheckOut() {
			if (checkOut != null) return checkOut;
			if (checkOutDate != null) return checkOutDate;
			throw new ConflictException("checkOutDate is required");
		}

		public int resolveGuestCount() {
			if (guestCount != null && guestCount > 0) return guestCount;
			if (numberOfGuests != null && numberOfGuests > 0) return numberOfGuests;
			throw new ConflictException("numberOfGuests must be greater than 0");
		}

		private static UUID parseUuid(String value, String fieldName) {
			try {
				return UUID.fromString(value);
			} catch (Exception ex) {
				throw new ConflictException(fieldName + " must be a valid UUID");
			}
		}
	}

	public record UpdateStatusRequest(
		@NotNull BookingStatus status
	) {}

	public record BookingResponse(
		String id,
		String userId,
		String roomInventoryId,
		String bookingReference,
		String hotelName,
		String hotelCity,
		String roomType,
		String roomNumber,
		String checkInDate,
		String checkOutDate,
		int numberOfGuests,
		int numberOfNights,
		double totalPrice,
		String checkIn,
		String checkOut,
		int guestCount,
		String status,
		String totalAmount,
		String currency
	) {
	}

	private BookingResponse toBookingResponse(com.hclhackathon.hotel.domain.BookingEntity booking) {
		var room = roomInventory.findById(booking.roomInventoryId).orElseThrow(() -> new NotFoundException("Room not found"));
		var roomType = roomTypes.findById(room.roomTypeId).orElseThrow(() -> new NotFoundException("Room type not found"));
		var hotel = hotels.findById(roomType.hotelId).orElseThrow(() -> new NotFoundException("Hotel not found"));
		var nights = (int) ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut);
		return new BookingResponse(
			booking.id.toString(),
			booking.userId.toString(),
			booking.roomInventoryId.toString(),
			booking.id.toString(),
			hotel.name,
			hotel.city,
			roomType.name,
			room.roomLabel,
			booking.checkIn.toString(),
			booking.checkOut.toString(),
			booking.guestCount,
			nights,
			booking.totalAmount.doubleValue(),
			booking.checkIn.toString(),
			booking.checkOut.toString(),
			booking.guestCount,
			booking.status.name(),
			booking.totalAmount.toPlainString(),
			booking.currency
		);
	}
}

