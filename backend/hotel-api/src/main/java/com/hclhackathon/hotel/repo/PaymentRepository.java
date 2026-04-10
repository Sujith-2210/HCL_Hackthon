package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.PaymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
	Optional<PaymentEntity> findByBookingId(UUID bookingId);
}

