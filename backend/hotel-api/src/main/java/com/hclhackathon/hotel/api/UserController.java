package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.repo.UserRepository;
import com.hclhackathon.hotel.security.AuthUser;
import com.hclhackathon.hotel.service.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserRepository users;

	public UserController(UserRepository users) {
		this.users = users;
	}

	@PreAuthorize("hasRole('CUSTOMER') or hasRole('RECEPTIONIST') or hasRole('ADMIN')")
	@GetMapping("/profile")
	public ApiResponse<UserProfileResponse> profile(Authentication auth) {
		var principal = requireAuthUser(auth);
		var user = users.findById(principal.getUserId()).orElseThrow(() -> new NotFoundException("User not found"));
		var names = splitName(user.fullName);
		return ApiResponse.of(new UserProfileResponse(
			user.id.toString(),
			user.email,
			names[0],
			names[1],
			null,
			user.role.name(),
			user.createdAt.toString()
		));
	}

	private static AuthUser requireAuthUser(Authentication auth) {
		if (auth == null || auth.getPrincipal() == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		if (!(auth.getPrincipal() instanceof AuthUser au)) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Not authenticated");
		return au;
	}

	private static String[] splitName(String fullName) {
		if (fullName == null || fullName.isBlank()) return new String[] {"", ""};
		var parts = fullName.trim().split("\\s+", 2);
		var firstName = parts[0];
		var lastName = parts.length > 1 ? parts[1] : "";
		return new String[] {firstName, lastName};
	}

	public record UserProfileResponse(
		String userId,
		String email,
		String firstName,
		String lastName,
		String phone,
		String role,
		String createdAt
	) {}
}
