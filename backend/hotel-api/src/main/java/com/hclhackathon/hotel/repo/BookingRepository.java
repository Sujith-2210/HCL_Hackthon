package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.BookingEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
	List<BookingEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
	List<BookingEntity> findAllByOrderByCreatedAtDesc();

	@Query("""
		select count(b)
		from BookingEntity b
		where b.roomInventoryId in :roomInventoryIds
			and b.status not in (
				com.hclhackathon.hotel.domain.BookingStatus.CANCELLED,
				com.hclhackathon.hotel.domain.BookingStatus.COMPLETED
			)
			and b.checkIn < :checkOut
			and b.checkOut > :checkIn
		""")
	long countActiveOverlappingBookings(
		@Param("roomInventoryIds") List<UUID> roomInventoryIds,
		@Param("checkIn") LocalDate checkIn,
		@Param("checkOut") LocalDate checkOut
	);
}
