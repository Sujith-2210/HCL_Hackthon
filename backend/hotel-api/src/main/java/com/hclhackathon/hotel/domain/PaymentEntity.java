package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {
	@Id
	public UUID id;

	@Column(name = "booking_id", nullable = false, unique = true)
	public UUID bookingId;

	@Column(nullable = false)
	public BigDecimal amount;

	@Column(nullable = false)
	public String currency;

	@Column(name = "method", nullable = false)
	public String method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public PaymentStatus status;

	@Column(name = "transaction_ref", nullable = false)
	public String transactionRef;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;
}

