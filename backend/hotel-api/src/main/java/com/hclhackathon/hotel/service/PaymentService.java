package com.hclhackathon.hotel.service;

import com.hclhackathon.hotel.domain.PaymentEntity;
import com.hclhackathon.hotel.domain.PaymentStatus;
import com.hclhackathon.hotel.domain.BookingStatus;
import com.hclhackathon.hotel.repo.BookingRepository;
import com.hclhackathon.hotel.repo.PaymentRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
	private final PaymentRepository payments;
	private final BookingRepository bookings;

	public PaymentService(PaymentRepository payments, BookingRepository bookings) {
		this.payments = payments;
		this.bookings = bookings;
	}

	@Transactional
	public PaymentEntity payBooking(UUID actorUserId, String actorRole, UUID bookingId, String method) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		var isPrivileged = "ADMIN".equalsIgnoreCase(actorRole) || "RECEPTIONIST".equalsIgnoreCase(actorRole);
		if (!isPrivileged && !booking.userId.equals(actorUserId)) throw new NotFoundException("Booking not found");
		if (booking.status == BookingStatus.CANCELLED) throw new ConflictException("Cancelled booking cannot be paid");
		if (booking.status == BookingStatus.COMPLETED) throw new ConflictException("Completed booking cannot be paid");

		var existing = payments.findByBookingId(bookingId).orElse(null);
		if (existing != null && existing.status == PaymentStatus.PAID) return existing;

		var payment = existing == null ? new PaymentEntity() : existing;
		payment.id = existing == null ? UUID.randomUUID() : existing.id;
		payment.bookingId = bookingId;
		payment.amount = booking.totalAmount;
		payment.currency = booking.currency;
		payment.method = method == null || method.isBlank() ? "UPI" : method;
		payment.status = PaymentStatus.PAID;
		payment.transactionRef = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		payment.createdAt = existing == null ? Instant.now() : existing.createdAt;

		return payments.save(payment);
	}

	public PaymentEntity getPaymentByBooking(UUID actorUserId, String actorRole, UUID bookingId) {
		var booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
		var isPrivileged = "ADMIN".equalsIgnoreCase(actorRole) || "RECEPTIONIST".equalsIgnoreCase(actorRole);
		if (!isPrivileged && !booking.userId.equals(actorUserId)) throw new NotFoundException("Booking not found");
		return payments.findByBookingId(bookingId).orElseThrow(() -> new NotFoundException("Payment not found"));
	}
}

