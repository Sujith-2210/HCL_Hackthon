package com.hotel.service;

import com.hotel.dto.response.HotelResponse;
import com.hotel.dto.response.RoomResponse;
import com.hotel.entity.Hotel;
import com.hotel.entity.Room;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HotelService {
    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
    }

    public List<HotelResponse> searchHotels(String city, LocalDate checkIn, LocalDate checkOut, int guests, String category) {
        log.info("Searching hotels: city={}, checkIn={}, checkOut={}, guests={}", city, checkIn, checkOut, guests);
        List<Hotel> hotels;
        if (checkIn != null && checkOut != null) {
            hotels = hotelRepository.searchAvailableHotels(city, checkIn, checkOut, guests, category);
        } else {
            hotels = city != null
                    ? hotelRepository.findByCityContainingIgnoreCaseAndActiveTrue(city)
                    : hotelRepository.findByActiveTrue();
        }
        return hotels.stream().map(this::toHotelResponse).toList();
    }

    public HotelResponse getHotelById(Long id) {
        log.info("Fetching hotel: {}", id);
        return toHotelResponse(hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id)));
    }

    public List<RoomResponse> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, int guests) {
        log.info("Fetching rooms for hotel: {}", hotelId);
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
        List<Room> rooms = (checkIn != null && checkOut != null)
                ? roomRepository.findAvailableRooms(hotelId, checkIn, checkOut, guests)
                : roomRepository.findByHotelIdAndAvailableTrue(hotelId);
        return rooms.stream().map(this::toRoomResponse).toList();
    }

    private HotelResponse toHotelResponse(Hotel hotel) {
        long availableRooms = hotel.getRooms() != null
                ? hotel.getRooms().stream().filter(Room::isAvailable).count() : 0;
        return HotelResponse.builder()
                .id(hotel.getId()).name(hotel.getName()).location(hotel.getLocation())
                .city(hotel.getCity()).country(hotel.getCountry()).description(hotel.getDescription())
                .imageUrl(hotel.getImageUrl()).rating(hotel.getRating()).pricePerNight(hotel.getPricePerNight())
                .amenities(hotel.getAmenities()).category(hotel.getCategory())
                .availableRooms((int) availableRooms).build();
    }

    private RoomResponse toRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId()).hotelId(room.getHotel().getId()).hotelName(room.getHotel().getName())
                .roomNumber(room.getRoomNumber()).roomType(room.getRoomType().name())
                .capacity(room.getCapacity()).pricePerNight(room.getPricePerNight())
                .description(room.getDescription()).amenities(room.getAmenities())
                .imageUrl(room.getImageUrl()).available(room.isAvailable()).build();
    }
}
