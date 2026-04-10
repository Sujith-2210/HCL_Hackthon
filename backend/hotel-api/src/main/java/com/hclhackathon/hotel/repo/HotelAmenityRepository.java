package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.HotelAmenityEntity;
import com.hclhackathon.hotel.domain.HotelAmenityEntity.HotelAmenityId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenityEntity, HotelAmenityId> {
	@Query("select ha.id.amenityId from HotelAmenityEntity ha where ha.id.hotelId = :hotelId")
	List<UUID> findAmenityIdsByHotelId(@Param("hotelId") UUID hotelId);
}

