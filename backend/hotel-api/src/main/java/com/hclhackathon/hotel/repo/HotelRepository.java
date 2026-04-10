package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.HotelEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<HotelEntity, UUID> {
	List<HotelEntity> findByCityIgnoreCase(String city);
}

