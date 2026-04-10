package com.hotel.repository;

import com.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityContainingIgnoreCaseAndActiveTrue(String city);

    List<Hotel> findByActiveTrue();

    @Query("""
            SELECT DISTINCT h FROM Hotel h
            JOIN h.rooms r
            WHERE h.active = true
            AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
            AND (:category IS NULL OR h.category = :category)
            AND r.available = true
            AND r.capacity >= :guests
            AND r.id NOT IN (
                SELECT b.room.id FROM Booking b
                WHERE b.status IN ('CONFIRMED', 'PENDING')
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            )
            """)
    List<Hotel> searchAvailableHotels(
            @Param("city") String city,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests,
            @Param("category") String category
    );
}
