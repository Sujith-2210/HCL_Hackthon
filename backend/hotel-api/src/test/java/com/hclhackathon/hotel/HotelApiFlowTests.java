package com.hclhackathon.hotel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hclhackathon.hotel.domain.HotelEntity;
import com.hclhackathon.hotel.domain.Role;
import com.hclhackathon.hotel.domain.RoomInventoryEntity;
import com.hclhackathon.hotel.domain.RoomTypeEntity;
import com.hclhackathon.hotel.domain.UserEntity;
import com.hclhackathon.hotel.repo.BookingRepository;
import com.hclhackathon.hotel.repo.HotelRepository;
import com.hclhackathon.hotel.repo.PaymentRepository;
import com.hclhackathon.hotel.repo.RoomInventoryRepository;
import com.hclhackathon.hotel.repo.RoomTypeRepository;
import com.hclhackathon.hotel.repo.UserRepository;
import com.hclhackathon.hotel.service.BookingService;
import com.hclhackathon.hotel.service.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HotelApiFlowTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BookingRepository bookings;

	@Autowired
	private PaymentRepository payments;

	@Autowired
	private RoomInventoryRepository roomInventory;

	@Autowired
	private RoomTypeRepository roomTypes;

	@Autowired
	private HotelRepository hotels;

	@Autowired
	private UserRepository users;

	private UUID roomTypeId;

	@BeforeEach
	void setUp() {
		payments.deleteAll();
		bookings.deleteAll();
		roomInventory.deleteAll();
		roomTypes.deleteAll();
		hotels.deleteAll();
		users.deleteAll();

		var hotel = new HotelEntity();
		hotel.id = UUID.randomUUID();
		hotel.name = "Test Suites";
		hotel.description = "Integration test hotel";
		hotel.address = "1 Test Street";
		hotel.city = "Chennai";
		hotel.country = "India";
		hotel.starRating = 4;
		hotel.createdAt = Instant.now();
		hotels.save(hotel);

		var roomType = new RoomTypeEntity();
		roomType.id = UUID.randomUUID();
		roomType.hotelId = hotel.id;
		roomType.name = "Deluxe";
		roomType.description = "Large room";
		roomType.maxOccupancy = 2;
		roomType.basePricePerNight = new BigDecimal("5000.00");
		roomType.currency = "INR";
		roomTypes.save(roomType);
		roomTypeId = roomType.id;

		var inventory = new RoomInventoryEntity();
		inventory.id = UUID.randomUUID();
		inventory.roomTypeId = roomType.id;
		inventory.roomLabel = "DLX-101";
		inventory.isActive = true;
		roomInventory.save(inventory);

		users.save(createUser("customer1@test.com", "Customer One", Role.CUSTOMER));
		users.save(createUser("customer2@test.com", "Customer Two", Role.CUSTOMER));
	}

	@Test
	void customerCanLoginCreateBookingAndFetchHistory() throws Exception {
		var token = login("customer1@test.com", "Password@123");
		var checkIn = LocalDate.now().plusDays(2);
		var checkOut = checkIn.plusDays(2);

		mockMvc.perform(post("/api/bookings")
				.contentType(APPLICATION_JSON)
				.header("Authorization", "Bearer " + token)
				.content("""
					{
					  "roomId": "%s",
					  "checkInDate": "%s",
					  "checkOutDate": "%s",
					  "numberOfGuests": 2
					}
					""".formatted(roomTypeId, checkIn, checkOut)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("PENDING"))
			.andExpect(jsonPath("$.data.hotelName").value("Test Suites"))
			.andExpect(jsonPath("$.data.totalPrice").value(10000.0));

		mockMvc.perform(get("/api/bookings/my")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].status").value("PENDING"));
	}

	@Test
	void pendingBookingsBlockOverlappingReservations() {
		var firstCustomerId = users.findByEmail("customer1@test.com").orElseThrow().id;
		var secondCustomerId = users.findByEmail("customer2@test.com").orElseThrow().id;
		var checkIn = LocalDate.now().plusDays(3);
		var checkOut = checkIn.plusDays(2);

		var firstBooking = bookingService.createBooking(firstCustomerId, Role.CUSTOMER.name(), roomTypeId, checkIn, checkOut, 2);

		assertThat(firstBooking.status.name()).isEqualTo("PENDING");
		assertThatThrownBy(() -> bookingService.createBooking(secondCustomerId, Role.CUSTOMER.name(), roomTypeId, checkIn, checkOut, 2))
			.isInstanceOf(ConflictException.class)
			.hasMessageContaining("No rooms available");
	}

	private UserEntity createUser(String email, String fullName, Role role) {
		var user = new UserEntity();
		user.id = UUID.randomUUID();
		user.email = email;
		user.fullName = fullName;
		user.passwordHash = passwordEncoder.encode("Password@123");
		user.role = role;
		user.createdAt = Instant.now();
		return user;
	}

	private String login(String email, String password) throws Exception {
		var response = mockMvc.perform(post("/api/auth/login")
				.contentType(APPLICATION_JSON)
				.content("""
					{
					  "email": "%s",
					  "password": "%s"
					}
					""".formatted(email, password)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.token").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		var token = body.path("data").path("token").asText();
		assertThat(token).isNotBlank();
		return token;
	}
}
