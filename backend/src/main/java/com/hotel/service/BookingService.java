package com.hotel.service;

import com.hotel.dto.request.BookingRequest;
import com.hotel.dto.response.BookingResponse;
import com.hotel.entity.*;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          UserRepository userRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        log.info("Creating booking for user: {}, room: {}", userEmail, request.getRoomId());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        validateRoomAvailability(room, request.getCheckInDate(), request.getCheckOutDate());
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference()).user(user).room(room)
                .checkInDate(request.getCheckInDate()).checkOutDate(request.getCheckOutDate())
                .numberOfGuests(request.getNumberOfGuests()).totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED).specialRequests(request.getSpecialRequests()).build();
        booking = bookingRepository.save(booking);
        log.info("Booking created: {}", booking.getBookingReference());
        emailService.sendBookingConfirmation(user, booking);
        return toBookingResponse(booking);
    }

    public List<BookingResponse> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toBookingResponse).toList();
    }

    public BookingResponse getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getEmail().equals(userEmail))
            throw new BadRequestException("You don't have access to this booking");
        return toBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id, String userEmail) {
        log.info("Cancelling booking: {} for user: {}", id, userEmail);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getEmail().equals(userEmail))
            throw new BadRequestException("You don't have access to this booking");
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BadRequestException("Booking is already cancelled");
        if (booking.getCheckInDate().isBefore(LocalDate.now()))
            throw new BadRequestException("Cannot cancel a past booking");
        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        log.info("Booking cancelled: {}", booking.getBookingReference());
        emailService.sendBookingCancellation(booking.getUser(), booking);
        return toBookingResponse(booking);
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) throw new BadRequestException("Check-out date must be after check-in date");
        if (checkIn.isBefore(LocalDate.now())) throw new BadRequestException("Check-in date cannot be in the past");
    }

    private void validateRoomAvailability(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) throw new BadRequestException("Room is not available");
        boolean conflict = room.getBookings() != null && room.getBookings().stream()
                .anyMatch(b -> (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING)
                        && b.getCheckInDate().isBefore(checkOut) && b.getCheckOutDate().isAfter(checkIn));
        if (conflict) throw new BadRequestException("Room is already booked for the selected dates");
    }

    private String generateBookingReference() {
        return "HB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BookingResponse toBookingResponse(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        Room room = booking.getRoom();
        Hotel hotel = room.getHotel();
        User user = booking.getUser();
        return BookingResponse.builder()
                .id(booking.getId()).bookingReference(booking.getBookingReference())
                .userId(user.getId()).userName(user.getFirstName() + " " + user.getLastName())
                .userEmail(user.getEmail()).roomId(room.getId()).roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType().name()).hotelId(hotel.getId()).hotelName(hotel.getName())
                .hotelCity(hotel.getCity()).checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate()).numberOfNights((int) nights)
                .numberOfGuests(booking.getNumberOfGuests()).totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name()).specialRequests(booking.getSpecialRequests())
                .createdAt(booking.getCreatedAt()).build();
    }
}
