package com.hclhackathon.hotel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SerpApiHotelService {
	private static final String BASE_URL = "https://serpapi.com/search.json";
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(8);

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String apiKey;
	private final String gl;
	private final String hl;
	private final String currency;
	private final Map<String, SerpHotel> cacheById = new ConcurrentHashMap<>();

	public SerpApiHotelService(
		ObjectMapper objectMapper,
		@Value("${app.serpapi.key:}") String apiKey,
		@Value("${app.serpapi.gl:us}") String gl,
		@Value("${app.serpapi.hl:en}") String hl,
		@Value("${app.serpapi.currency:USD}") String currency
	) {
		this.objectMapper = objectMapper;
		this.httpClient = HttpClient.newHttpClient();
		this.apiKey = apiKey == null ? "" : apiKey.trim();
		this.gl = gl;
		this.hl = hl;
		this.currency = currency;
	}

	public boolean isEnabled() {
		return !apiKey.isBlank();
	}

	public List<SerpHotel> search(String query, LocalDate checkIn, LocalDate checkOut, Integer guests) {
		if (!isEnabled()) return List.of();

		var safeQuery = (query == null || query.isBlank()) ? "Hotels" : query.trim();
		var adults = guests == null || guests < 1 ? 2 : guests;
		var checkInDate = checkIn == null ? LocalDate.now().plusDays(1) : checkIn;
		var checkOutDate = checkOut == null ? checkInDate.plusDays(1) : checkOut;

		var url = BASE_URL
			+ "?engine=google_hotels"
			+ "&q=" + encode(safeQuery)
			+ "&gl=" + encode(gl)
			+ "&hl=" + encode(hl)
			+ "&currency=" + encode(currency)
			+ "&check_in_date=" + encode(checkInDate.toString())
			+ "&check_out_date=" + encode(checkOutDate.toString())
			+ "&adults=" + adults
			+ "&children=0"
			+ "&api_key=" + encode(apiKey);

		var root = fetchJson(url);
		if (root == null) return List.of();
		var hotels = new ArrayList<SerpHotel>();
		addProperties(hotels, root.path("properties"), safeQuery);
		addProperties(hotels, root.path("ads"), safeQuery);
		hotels.forEach(hotel -> cacheById.put(hotel.id(), hotel));
		return hotels;
	}

	public SerpHotel getById(String id, LocalDate checkIn, LocalDate checkOut, Integer guests) {
		var cached = cacheById.get(id);
		if (cached != null) return cached;

		var token = extractToken(id);
		var adults = guests == null || guests < 1 ? 2 : guests;
		var checkInDate = checkIn == null ? LocalDate.now().plusDays(1) : checkIn;
		var checkOutDate = checkOut == null ? checkInDate.plusDays(1) : checkOut;

		var url = BASE_URL
			+ "?engine=google_hotels"
			+ "&property_token=" + encode(token)
			+ "&gl=" + encode(gl)
			+ "&hl=" + encode(hl)
			+ "&currency=" + encode(currency)
			+ "&check_in_date=" + encode(checkInDate.toString())
			+ "&check_out_date=" + encode(checkOutDate.toString())
			+ "&adults=" + adults
			+ "&children=0"
			+ "&api_key=" + encode(apiKey);

		var root = fetchJson(url);
		if (root == null) throw new NotFoundException("Hotel not found");
		var hotel = toSerpHotel(root, "Hotel");
		if (hotel.propertyToken().isBlank()) throw new NotFoundException("Hotel not found");
		cacheById.put(hotel.id(), hotel);
		return hotel;
	}

	private void addProperties(List<SerpHotel> target, JsonNode nodes, String fallbackQuery) {
		if (nodes == null || !nodes.isArray()) return;
		for (var node : nodes) target.add(toSerpHotel(node, fallbackQuery));
	}

	private SerpHotel toSerpHotel(JsonNode node, String fallbackQuery) {
		var token = text(node, "property_token");
		var id = "serp::" + token;
		var name = text(node, "name");
		var category = text(node, "type");
		if (category.isBlank()) category = "Hotel";

		var price = number(node.path("rate_per_night").path("extracted_lowest"));
		if (price <= 0) price = number(node.path("extracted_price"));
		if (price <= 0) price = number(node.path("total_rate").path("extracted_lowest"));

		var rating = number(node.path("overall_rating"));
		var amenities = csv(node.path("amenities"));
		var description = text(node, "description");
		var city = fallbackQuery;
		var country = gl.toUpperCase();
		var location = city + ", " + country;
		var imageUrl = firstImage(node);

		return new SerpHotel(
			id,
			token,
			name,
			description,
			city,
			country,
			location,
			rating,
			amenities,
			price,
			1,
			imageUrl,
			category
		);
	}

	private JsonNode fetchJson(String url) {
		try {
			var request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) return null;
			return objectMapper.readTree(response.body());
		} catch (Exception ex) {
			return null;
		}
	}

	private static String extractToken(String id) {
		if (id == null || !id.startsWith("serp::")) throw new NotFoundException("Hotel not found");
		var token = id.substring("serp::".length());
		if (token.isBlank()) throw new NotFoundException("Hotel not found");
		return token;
	}

	private static String firstImage(JsonNode node) {
		var directThumb = text(node, "thumbnail");
		if (!directThumb.isBlank()) return directThumb;
		var images = node.path("images");
		if (!images.isArray() || images.isEmpty()) return null;
		var first = images.get(0);
		var original = text(first, "original_image");
		if (!original.isBlank()) return original;
		var thumb = text(first, "thumbnail");
		if (!thumb.isBlank()) return thumb;
		return null;
	}

	private static String csv(JsonNode node) {
		if (node == null || !node.isArray()) return "";
		var values = new ArrayList<String>();
		for (var entry : node) {
			var value = entry.asText("").trim();
			if (!value.isBlank()) values.add(value);
		}
		return String.join(", ", values);
	}

	private static String text(JsonNode node, String field) {
		if (node == null || node.isMissingNode()) return "";
		return node.path(field).asText("").trim();
	}

	private static double number(JsonNode node) {
		if (node == null || node.isMissingNode() || node.isNull()) return 0d;
		return node.asDouble(0d);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	public record SerpHotel(
		String id,
		String propertyToken,
		String name,
		String description,
		String city,
		String country,
		String location,
		double rating,
		String amenities,
		double pricePerNight,
		long availableRooms,
		String imageUrl,
		String category
	) {}
}
