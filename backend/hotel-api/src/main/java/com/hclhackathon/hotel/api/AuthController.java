package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.domain.Role;
import com.hclhackathon.hotel.domain.UserEntity;
import com.hclhackathon.hotel.repo.UserRepository;
import com.hclhackathon.hotel.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		var normalizedEmail = request.email().trim().toLowerCase();
		if (users.findByEmail(normalizedEmail).isPresent())
			throw new com.hclhackathon.hotel.service.ConflictException("Email already registered");

		var firstName = request.firstName() == null ? "" : request.firstName().trim();
		var lastName = request.lastName() == null ? "" : request.lastName().trim();
		var fullName = buildFullName(firstName, lastName, request.fullName());

		var user = new UserEntity();
		user.id = UUID.randomUUID();
		user.email = normalizedEmail;
		user.fullName = fullName;
		user.passwordHash = passwordEncoder.encode(request.password());
		user.role = Role.CUSTOMER;
		user.createdAt = Instant.now();

		users.save(user);
		log.info("user_registered email={} role={}", user.email, user.role);

		var token = jwtService.issueToken(user.id, user.email, user.role.name());
		return ApiResponse.of(AuthResponse.fromUser(token, user));
	}

	@PostMapping("/login")
	public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		var normalizedEmail = request.email().trim().toLowerCase();
		var user = users.findByEmail(normalizedEmail)
			.orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

		if (!passwordEncoder.matches(request.password(), user.passwordHash))
			throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials");

		log.info("user_login email={} role={}", user.email, user.role);
		var token = jwtService.issueToken(user.id, user.email, user.role.name());
		return ApiResponse.of(AuthResponse.fromUser(token, user));
	}

	public record RegisterRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		String firstName,
		String lastName,
		String fullName,
		String phone
	) {}

	public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password
	) {}

	public record AuthResponse(
		String token,
		String userId,
		String email,
		String fullName,
		String firstName,
		String lastName,
		String role
	) {
		public static AuthResponse fromUser(String token, UserEntity user) {
			var names = splitName(user.fullName);
			return new AuthResponse(token, user.id.toString(), user.email, user.fullName, names[0], names[1], user.role.name());
		}
	}

	private static String buildFullName(String firstName, String lastName, String fallbackFullName) {
		var joined = (firstName + " " + lastName).trim();
		if (!joined.isBlank()) return joined;
		if (fallbackFullName != null && !fallbackFullName.trim().isBlank()) return fallbackFullName.trim();
		throw new com.hclhackathon.hotel.service.ConflictException("First name and last name are required");
	}

	private static String[] splitName(String fullName) {
		if (fullName == null || fullName.isBlank()) return new String[] {"", ""};
		var parts = fullName.trim().split("\\s+", 2);
		var firstName = parts[0];
		var lastName = parts.length > 1 ? parts[1] : "";
		return new String[] {firstName, lastName};
	}
}

