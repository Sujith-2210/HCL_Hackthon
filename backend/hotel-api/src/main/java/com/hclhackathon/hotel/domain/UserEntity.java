package com.hclhackathon.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
	@Id
	public UUID id;

	@Column(nullable = false, unique = true)
	public String email;

	@Column(name = "password_hash", nullable = false)
	public String passwordHash;

	@Column(name = "full_name", nullable = false)
	public String fullName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public Role role;

	@Column(name = "created_at", nullable = false)
	public Instant createdAt;
}

