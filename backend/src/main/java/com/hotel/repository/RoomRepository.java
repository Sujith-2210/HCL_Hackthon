package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelIdAndAvailableTrue(Long hotelId);

    @Query("""
            SELECT r FROM Room r
            WHERE r.hotel.id = :hotelId
            AND r.available = true
            AND r.capacity >= :guests
            AND r.id NOT IN (
                SELECT b.room.id FROM Booking b
                WHERE b.status IN ('CONFIRMED', 'PENDING')
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            )
            """)
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests
    );
}
