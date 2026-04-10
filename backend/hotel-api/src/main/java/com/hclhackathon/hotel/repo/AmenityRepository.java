package com.hclhackathon.hotel.repo;

import com.hclhackathon.hotel.domain.AmenityEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<AmenityEntity, UUID> {
	List<AmenityEntity> findByIdIn(List<UUID> ids);
}

