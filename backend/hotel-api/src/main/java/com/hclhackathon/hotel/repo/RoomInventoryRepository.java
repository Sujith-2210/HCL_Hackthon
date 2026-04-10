package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.RoomInventoryEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomInventoryRepository extends JpaRepository<RoomInventoryEntity, UUID> {
	List<RoomInventoryEntity> findByRoomTypeIdAndIsActiveTrue(UUID roomTypeId);

	@Query("""
		select ri
		from RoomInventoryEntity ri
		where ri.roomTypeId = :roomTypeId
			and ri.isActive = true
			and ri.id not in (
				select b.roomInventoryId
				from BookingEntity b
				where b.status not in (
						com.hclhackathon.hotel.domain.BookingStatus.CANCELLED,
						com.hclhackathon.hotel.domain.BookingStatus.COMPLETED
					)
					and b.checkIn < :checkOut
					and b.checkOut > :checkIn
			)
		""")
	List<RoomInventoryEntity> findAvailableRooms(
		@Param("roomTypeId") UUID roomTypeId,
		@Param("checkIn") LocalDate checkIn,
		@Param("checkOut") LocalDate checkOut
	);
}
