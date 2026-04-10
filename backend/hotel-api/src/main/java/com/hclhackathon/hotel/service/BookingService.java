package com.hclhackathon.hotel.service;

import com.hclhackathon.hotel.domain.BookingEntity;
import com.hclhackathon.hotel.domain.BookingStatus;
import com.hclhackathon.hotel.domain.RoomTypeEntity;
import com.hclhackathon.hotel.repo.BookingRepository;
import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import com.hclhackathon.hotel.repo.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
	private static final Logger log = LoggerFactory.getLogger(BookingService.class);

	private final BookingRepository bookings;
	private final UserRepository users;
	private final RoomTypeRepository roomTypes;
	private final RoomInventoryRepository roomInventory;
	private final EmailService email;

	public BookingService(
		BookingRepository bookings,
		UserRepository users,
		RoomTypeRepository roomTypes,
		RoomInventoryRepository roomInventory,
		EmailService email
	) {
		this.bookings = bookings;
		this.users = users;
		this.roomTypes = roomTypes;
		this.roomInventory = roomInventory;
		this.email = email;
	}

	@Transactional
	public BookingEntity createBooking(UUID userId, String actorRole, UUID roomTypeId, LocalDate checkIn, LocalDate checkOut, int guestCount) {
		if (checkIn == null || checkOut == null) throw new ConflictException("Check-in and check-out are required");
		if (!checkOut.isAfter(checkIn)) throw new ConflictException("Check-out must be after check-in");
		if (checkIn.isBefore(LocalDate.now())) throw new ConflictException("Check-in cannot be in the past");

		var user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
		var roomType = roomTypes.findById(roomTypeId).orElseThrow(() -> new NotFoundException("Room type not found"));
		if (guestCount <= 0) throw new ConflictException("Guest count must be positive");
		if (guestCount > roomType.maxOccupancy) throw new ConflictException("Guest count exceeds max occupancy");

		var availableRooms = roomInventory.findAvailableRooms(roomTypeId, checkIn, checkOut);
		if (availableRooms.isEmpty()) throw new ConflictException("No rooms available for the selected dates");

		var nights = ChronoUnit.DAYS.between(checkIn, checkOut);
		var totalAmount = roomType.basePricePerNight.multiply(BigDecimal.valueOf(nights));

		var booking = new BookingEntity();
		booking.id = UUID.randomUUID();
		booking.userId = userId;
		booking.roomInventoryId = availableRooms.get(0).id;
		booking.checkIn = checkIn;
		booking.checkOut = checkOut;
		booking.guestCount = guestCount;
		booking.status = "CUSTOMER".equalsIgnoreCase(actorRole) ? BookingStatus.PENDING : BookingStatus.CONFIRMED;
		booking.totalAmount = totalAmount;
		booking.currency = roomType.currency;
		booking.createdAt = Instant.now();
		booking.updatedAt = Instant.now();

		bookings.save(booking);
		log.info("booking_created bookingId={} userId={} role={} roomTypeId={} roomInventoryId={} checkIn={} checkOut={} status={}",
			booking.id, booking.userId, actorRole, roomTypeId, booking.roomInventoryId, checkIn, checkOut, booking.status);

		if (booking.status == BookingStatus.CONFIRMED) {
			email.sendBookingConfirmationEmail(user.email, booking.id.toString());
		} else {
			email.sendBookingReceivedEmail(user.email, booking.id.toString());
		}
		return booking;
	}

	public List<BookingEntity> getBookingHistory(UUID userId) {
		return bookings.findByUserIdOrderByCreatedAtDesc(userId);
	}

	public List<BookingEntity> getAllBookings() {
		return bookings.findAllByOrderByCreatedAtDesc();
	}

	@Transactional
	public BookingEntity cancelBooking(UUID userId, UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		if (!booking.userId.equals(userId)) throw new NotFoundException("Booking not found");
		if (booking.status == BookingStatus.CANCELLED) return booking;
		if (booking.status == BookingStatus.COMPLETED) throw new ConflictException("Completed booking cannot be cancelled");
		if (booking.status == BookingStatus.CHECKED_IN) throw new ConflictException("Checked-in booking cannot be cancelled");

		booking.status = BookingStatus.CANCELLED;
		booking.updatedAt = Instant.now();
		bookings.save(booking);

		var user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
		log.info("booking_cancelled bookingId={} userId={}", bookingId, userId);
		email.sendBookingCancellationEmail(user.email, booking.id.toString());
		return booking;
	}

	@Transactional
	public BookingEntity cancelBookingByStaff(UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		if (booking.status == BookingStatus.CANCELLED) return booking;
		if (booking.status == BookingStatus.COMPLETED) throw new ConflictException("Completed booking cannot be cancelled");
		if (booking.status == BookingStatus.CHECKED_IN) throw new ConflictException("Checked-in booking cannot be cancelled");

		booking.status = BookingStatus.CANCELLED;
		booking.updatedAt = Instant.now();
		bookings.save(booking);
		return booking;
	}

	@Transactional
	public BookingEntity confirmBooking(UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		if (booking.status == BookingStatus.CANCELLED) throw new ConflictException("Cancelled booking cannot be confirmed");
		if (booking.status == BookingStatus.CONFIRMED) return booking;
		if (booking.status == BookingStatus.COMPLETED) throw new ConflictException("Completed booking cannot be confirmed");
		if (booking.status == BookingStatus.CHECKED_IN) throw new ConflictException("Checked-in booking cannot be confirmed");
		if (booking.status != BookingStatus.PENDING) throw new ConflictException("Only pending bookings can be confirmed");
		booking.status = BookingStatus.CONFIRMED;
		booking.updatedAt = Instant.now();
		return bookings.save(booking);
	}

	@Transactional
	public BookingEntity checkInBooking(UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		if (booking.status == BookingStatus.CANCELLED) throw new ConflictException("Cancelled booking cannot check-in");
		if (booking.status == BookingStatus.COMPLETED) throw new ConflictException("Completed booking cannot check-in");
		if (booking.status == BookingStatus.CHECKED_IN) return booking;
		if (booking.status == BookingStatus.PENDING) throw new ConflictException("Booking must be confirmed before check-in");
		if (booking.status != BookingStatus.CONFIRMED) throw new ConflictException("Only confirmed bookings can be checked in");
		if (LocalDate.now().isBefore(booking.checkIn)) throw new ConflictException("Cannot check-in before booking start date");
		booking.status = BookingStatus.CHECKED_IN;
		booking.updatedAt = Instant.now();
		return bookings.save(booking);
	}

	@Transactional
	public BookingEntity completeBooking(UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		if (booking.status == BookingStatus.CANCELLED) throw new ConflictException("Cancelled booking cannot be completed");
		if (booking.status == BookingStatus.COMPLETED) return booking;
		if (booking.status == BookingStatus.PENDING) throw new ConflictException("Pending booking cannot be completed");
		if (booking.status == BookingStatus.CONFIRMED) throw new ConflictException("Check-in is required before check-out");
		if (booking.status != BookingStatus.CHECKED_IN) throw new ConflictException("Only checked-in bookings can be completed");
		if (LocalDate.now().isBefore(booking.checkIn)) throw new ConflictException("Cannot check-out before check-in date");

		booking.status = BookingStatus.COMPLETED;
		booking.updatedAt = Instant.now();
		bookings.save(booking);

		log.info("booking_completed bookingId={}", bookingId);
		return booking;
	}

	public RoomTypeEntity getRoomType(UUID roomTypeId) {
		return roomTypes.findById(roomTypeId).orElseThrow(() -> new NotFoundException("Room type not found"));
	}
}
