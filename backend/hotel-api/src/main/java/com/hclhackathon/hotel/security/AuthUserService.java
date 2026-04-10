package com.hclhackathon.hotel.security;

import com.hclhackathon.hotel.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService implements UserDetailsService {
	private final UserRepository users;

	public AuthUserService(UserRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var user = users.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return new AuthUser(user.id, user.email, user.passwordHash, user.role.name());
	}
}

