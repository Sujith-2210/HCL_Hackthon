package com.hclhackathon.hotel.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		var requestId = request.getHeader("X-Request-Id");
		if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();

		response.setHeader("X-Request-Id", requestId);

		var start = System.currentTimeMillis();
		try {
			filterChain.doFilter(request, response);
		} finally {
			var durationMs = System.currentTimeMillis() - start;
			log.info("request requestId={} method={} path={} status={} durationMs={}",
				requestId, request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
		}
	}
}

