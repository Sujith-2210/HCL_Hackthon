package com.hclhackathon.hotel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final SecretKey key;
	private final String issuer;
	private final long expirationSeconds;

	public JwtService(
		@Value("${app.jwt.secret}") String secret,
		@Value("${app.jwt.issuer}") String issuer,
		@Value("${app.jwt.expiration-seconds}") long expirationSeconds
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.issuer = issuer;
		this.expirationSeconds = expirationSeconds;
	}

	public String issueToken(UUID userId, String email, String role) {
		var now = Instant.now();
		var exp = now.plusSeconds(expirationSeconds);

		return Jwts.builder()
			.issuer(issuer)
			.subject(userId.toString())
			.claim("email", email)
			.claim("role", role)
			.issuedAt(Date.from(now))
			.expiration(Date.from(exp))
			.signWith(key)
			.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.requireIssuer(issuer)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}

