package com.hclhackathon.hotel.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		var header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		var token = header.substring("Bearer ".length()).trim();
		if (token.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			Claims claims = jwtService.parseClaims(token);
			var userId = UUID.fromString(claims.getSubject());
			var email = String.valueOf(claims.get("email"));
			var role = String.valueOf(claims.get("role"));

			var auth = new UsernamePasswordAuthenticationToken(
				new AuthUser(userId, email, "", role),
				null,
				java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))
			);
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception ignored) {
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}
}

