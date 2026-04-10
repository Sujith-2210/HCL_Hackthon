package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.service.PaymentService;
import com.hclhackathon.hotel.security.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
	private final PaymentService payments;

	public PaymentController(PaymentService payments) {
		this.payments = payments;
	}

	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping
	public ApiResponse<PaymentResponse> pay(Authentication auth, @Valid @RequestBody PayBookingRequest request) {
		var actor = requireAuthUser(auth);
		return ApiResponse.of(PaymentResponse.from(payments.payBooking(actor.getUserId(), actor.getRole(), request.bookingId(), request.method())));
	}

	@PreAuthorize("hasRole('CUSTOMER') or hasRole('RECEPTIONIST') or hasRole('ADMIN')")
	@GetMapping("/booking/{bookingId}")
	public ApiResponse<PaymentResponse> getByBooking(Authentication auth, @PathVariable UUID bookingId) {
		var actor = requireAuthUser(auth);
		return ApiResponse.of(PaymentResponse.from(payments.getPaymentByBooking(actor.getUserId(), actor.getRole(), bookingId)));
	}

	private static AuthUser requireAuthUser(Authentication auth) {
		if (auth == null || auth.getPrincipal() == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		if (!(auth.getPrincipal() instanceof AuthUser au)) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		return au;
	}

	public record PayBookingRequest(
		@NotNull UUID bookingId,
		String method
	) {}

	public record PaymentResponse(
		String id,
		String bookingId,
		String amount,
		String currency,
		String method,
		String status,
		String transactionRef
	) {
		public static PaymentResponse from(com.hclhackathon.hotel.domain.PaymentEntity p) {
			return new PaymentResponse(
				p.id.toString(),
				p.bookingId.toString(),
				p.amount.toPlainString(),
				p.currency,
				p.method,
				p.status.name(),
				p.transactionRef
			);
		}
	}
}

