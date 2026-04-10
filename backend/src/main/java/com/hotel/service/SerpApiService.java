package com.hotel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.dto.response.SerpApiHotelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service that integrates with SerpAPI's Google Hotels engine
 * to fetch real-time hotel data from Google Hotels.
 */
@Service
public class SerpApiService {
    private static final Logger log = LoggerFactory.getLogger(SerpApiService.class);
    private static final String SERPAPI_BASE_URL = "https://serpapi.com/search.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.serpapi.api-key}")
    private String apiKey;

    public SerpApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search hotels via SerpAPI Google Hotels engine.
     *
     * @param query      Search query (e.g. "hotels in Mumbai")
     * @param checkIn    Check-in date (yyyy-MM-dd)
     * @param checkOut   Check-out date (yyyy-MM-dd)
     * @param adults     Number of adults
     * @param currency   Currency code (e.g. "INR", "USD")
     * @return List of hotel results
     */
    public List<SerpApiHotelResponse> searchHotels(String query, String checkIn, String checkOut,
                                                    int adults, String currency) {
        try {
            String url = buildSearchUrl(query, checkIn, checkOut, adults, currency, null);
            log.info("SerpAPI Hotel Search: query={}, checkIn={}, checkOut={}, adults={}, currency={}",
                    query, checkIn, checkOut, adults, currency);

            String jsonResponse = executeRequest(url);
            return parseHotelResults(jsonResponse, currency);
        } catch (Exception e) {
            log.error("SerpAPI hotel search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get detailed information about a specific hotel property.
     *
     * @param propertyToken The property token from a previous search
     * @param query         Original search query
     * @param checkIn       Check-in date
     * @param checkOut      Check-out date
     * @param adults        Number of adults
     * @param currency      Currency code
     * @return Hotel details or null if not found
     */
    public SerpApiHotelResponse getHotelDetails(String propertyToken, String query,
                                                 String checkIn, String checkOut,
                                                 int adults, String currency) {
        try {
            String url = buildSearchUrl(query, checkIn, checkOut, adults, currency, propertyToken);
            log.info("SerpAPI Hotel Details: token={}", propertyToken);

            String jsonResponse = executeRequest(url);
            JsonNode root = objectMapper.readTree(jsonResponse);

            // When property_token is provided, the response contains detailed info
            JsonNode properties = root.path("properties");
            if (properties.isArray() && !properties.isEmpty()) {
                return parseProperty(properties.get(0), currency);
            }

            return null;
        } catch (Exception e) {
            log.error("SerpAPI hotel details failed: {}", e.getMessage(), e);
            return null;
        }
    }

    // ---- Private helpers ----

    private String buildSearchUrl(String query, String checkIn, String checkOut,
                                   int adults, String currency, String propertyToken) {
        StringBuilder sb = new StringBuilder(SERPAPI_BASE_URL);
        sb.append("?engine=google_hotels");
        sb.append("&q=").append(encode(query));
        sb.append("&check_in_date=").append(encode(checkIn));
        sb.append("&check_out_date=").append(encode(checkOut));
        sb.append("&adults=").append(adults);
        sb.append("&currency=").append(encode(currency != null ? currency : "INR"));
        sb.append("&gl=in"); // Default to India locale
        sb.append("&hl=en");
        if (propertyToken != null && !propertyToken.isEmpty()) {
            sb.append("&property_token=").append(encode(propertyToken));
        }
        sb.append("&api_key=").append(encode(apiKey));
        return sb.toString();
    }

    private String executeRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("SerpAPI returned status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("SerpAPI request failed with status: " + response.statusCode());
        }

        return response.body();
    }

    private List<SerpApiHotelResponse> parseHotelResults(String jsonResponse, String currency) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        List<SerpApiHotelResponse> results = new ArrayList<>();

        // Parse main "properties" array (organic results)
        JsonNode properties = root.path("properties");
        if (properties.isArray()) {
            for (JsonNode prop : properties) {
                SerpApiHotelResponse hotel = parseProperty(prop, currency);
                if (hotel != null) {
                    results.add(hotel);
                }
            }
        }

        log.info("SerpAPI returned {} hotel results", results.size());
        return results;
    }

    private SerpApiHotelResponse parseProperty(JsonNode prop, String currency) {
        try {
            String name = prop.path("name").asText("");
            if (name.isEmpty()) return null;

            // Extract pricing
            double pricePerNight = 0;
            double totalPrice = 0;
            JsonNode rateNode = prop.path("rate_per_night");
            if (!rateNode.isMissingNode()) {
                pricePerNight = rateNode.path("extracted_lowest").asDouble(0);
            }
            JsonNode totalNode = prop.path("total_rate");
            if (!totalNode.isMissingNode()) {
                totalPrice = totalNode.path("extracted_lowest").asDouble(0);
            }

            // Extract GPS coordinates
            double lat = 0, lng = 0;
            JsonNode gps = prop.path("gps_coordinates");
            if (!gps.isMissingNode()) {
                lat = gps.path("latitude").asDouble(0);
                lng = gps.path("longitude").asDouble(0);
            }

            // Extract images
            List<String> images = new ArrayList<>();
            JsonNode imagesNode = prop.path("images");
            if (imagesNode.isArray()) {
                for (JsonNode img : imagesNode) {
                    String original = img.path("original_image").asText("");
                    if (!original.isEmpty()) {
                        images.add(original);
                    } else {
                        String thumb = img.path("thumbnail").asText("");
                        if (!thumb.isEmpty()) images.add(thumb);
                    }
                }
            }

            // Extract thumbnail
            String thumbnail = "";
            if (!images.isEmpty()) {
                thumbnail = images.get(0);
            } else {
                thumbnail = prop.path("thumbnail").asText("");
            }

            // Extract amenities
            List<String> amenities = new ArrayList<>();
            JsonNode amenitiesNode = prop.path("amenities");
            if (amenitiesNode.isArray()) {
                for (JsonNode a : amenitiesNode) {
                    amenities.add(a.asText());
                }
            }

            // Extract nearby places
            List<SerpApiHotelResponse.NearbyPlace> nearbyPlaces = new ArrayList<>();
            JsonNode nearbyNode = prop.path("nearby_places");
            if (nearbyNode.isArray()) {
                for (JsonNode nb : nearbyNode) {
                    String placeName = nb.path("name").asText("");
                    JsonNode transports = nb.path("transportations");
                    if (transports.isArray() && !transports.isEmpty()) {
                        JsonNode first = transports.get(0);
                        nearbyPlaces.add(new SerpApiHotelResponse.NearbyPlace(
                                placeName,
                                first.path("type").asText(""),
                                first.path("duration").asText("")
                        ));
                    }
                }
            }

            return SerpApiHotelResponse.builder()
                    .name(name)
                    .description(prop.path("description").asText(""))
                    .type(prop.path("type").asText("hotel"))
                    .latitude(lat)
                    .longitude(lng)
                    .checkInTime(prop.path("check_in_time").asText(""))
                    .checkOutTime(prop.path("check_out_time").asText(""))
                    .overallRating(prop.path("overall_rating").asDouble(0))
                    .reviews(prop.path("reviews").asInt(0))
                    .hotelClass(prop.path("hotel_class").asText(""))
                    .extractedHotelClass(prop.path("extracted_hotel_class").asInt(0))
                    .pricePerNight(pricePerNight)
                    .totalPrice(totalPrice)
                    .currency(currency != null ? currency : "INR")
                    .thumbnail(thumbnail)
                    .images(images)
                    .amenities(amenities)
                    .nearbyPlaces(nearbyPlaces)
                    .propertyToken(prop.path("property_token").asText(""))
                    .link(prop.path("link").asText(""))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse hotel property: {}", e.getMessage());
            return null;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
