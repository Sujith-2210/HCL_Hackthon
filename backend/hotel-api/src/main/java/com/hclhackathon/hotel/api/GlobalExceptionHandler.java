package com.hclhackathon.hotel.api;

import com.hclhackathon.hotel.service.ConflictException;
import com.hclhackathon.hotel.service.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
			.map(GlobalExceptionHandler::toFieldError)
			.toList();

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiError(
			Instant.now(),
			HttpStatus.UNPROCESSABLE_ENTITY.value(),
			"validation_error",
			"Validation failed",
			request.getRequestURI(),
			fieldErrors
		));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiError(
			Instant.now(),
			HttpStatus.UNPROCESSABLE_ENTITY.value(),
			"validation_error",
			ex.getMessage(),
			request.getRequestURI(),
			List.of()
		));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
			Instant.now(),
			HttpStatus.BAD_REQUEST.value(),
			"bad_request",
			ex.getMessage(),
			request.getRequestURI(),
			List.of()
		));
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
			Instant.now(),
			HttpStatus.NOT_FOUND.value(),
			"not_found",
			ex.getMessage(),
			request.getRequestURI(),
			List.of()
		));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
			Instant.now(),
			HttpStatus.CONFLICT.value(),
			"conflict",
			ex.getMessage(),
			request.getRequestURI(),
			List.of()
		));
	}

	@ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
	public ResponseEntity<ApiError> handleAuth(Exception ex, HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
			Instant.now(),
			HttpStatus.UNAUTHORIZED.value(),
			"unauthorized",
			ex.getMessage(),
			request.getRequestURI(),
			List.of()
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest request) {
		log.error("Unhandled error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
			Instant.now(),
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			"internal_error",
			"Something went wrong. Please try again.",
			request.getRequestURI(),
			List.of()
		));
	}

	private static ApiError.FieldError toFieldError(FieldError fe) {
		var message = fe.getDefaultMessage();
		if (message == null) message = "Invalid value";
		return new ApiError.FieldError(fe.getField(), message);
	}
}

