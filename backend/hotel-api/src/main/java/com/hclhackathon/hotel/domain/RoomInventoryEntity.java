package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "room_inventory")
public class RoomInventoryEntity {
	@Id
	public UUID id;

	@Column(name = "room_type_id", nullable = false)
	public UUID roomTypeId;

	@Column(name = "room_label", nullable = false)
	public String roomLabel;

	@Column(name = "is_active", nullable = false)
	public Boolean isActive;
}

