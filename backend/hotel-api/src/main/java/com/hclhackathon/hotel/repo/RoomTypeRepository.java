package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.RoomTypeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomTypeEntity, UUID> {
	List<RoomTypeEntity> findByHotelId(UUID hotelId);
}

