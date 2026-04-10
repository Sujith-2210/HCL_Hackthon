-- =====================================================
-- Hotel Booking Application – Database Schema
-- =====================================================

CREATE DATABASE IF NOT EXISTS hotel_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_booking;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    INDEX idx_users_email (email)
);

-- Hotels table
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    rating DOUBLE,
    price_per_night DECIMAL(10, 2) NOT NULL,
    amenities TEXT,
    category VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hotels_city (city),
    INDEX idx_hotels_active (active)
);

-- Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type ENUM('SINGLE', 'DOUBLE', 'TWIN', 'SUITE', 'DELUXE', 'PENTHOUSE') NOT NULL,
    capacity INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    description TEXT,
    amenities TEXT,
    image_url VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    INDEX idx_rooms_hotel (hotel_id),
    INDEX idx_rooms_available (available)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    special_requests TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_bookings_user (user_id),
    INDEX idx_bookings_room (room_id),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_dates (check_in_date, check_out_date)
);

-- =====================================================
-- Sample Data
-- =====================================================

INSERT INTO hotels (name, location, city, country, description, image_url, rating, price_per_night, amenities, category) VALUES
('The Grand Palace', '12 Palace Road, Central District', 'Mumbai', 'India', 'An iconic luxury hotel with world-class amenities and breathtaking city views.', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800', 4.8, 8500.00, 'WiFi,Pool,Spa,Gym,Restaurant,Bar,Room Service,Valet Parking', 'LUXURY'),
('Sea View Resort', 'Marine Drive, Beachfront', 'Goa', 'India', 'Stunning beachfront resort with panoramic ocean views and world-class dining.', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800', 4.6, 6500.00, 'WiFi,Pool,Beach Access,Spa,Restaurant,Water Sports', 'LUXURY'),
('City Center Inn', '45 MG Road, Business District', 'Bangalore', 'India', 'Modern hotel in the heart of Bangalore, perfect for business travelers.', 'https://images.unsplash.com/photo-1455587734955-081b22074882?w=800', 4.3, 3500.00, 'WiFi,Gym,Restaurant,Conference Room,Business Center', 'BOUTIQUE'),
('Heritage Haveli', 'Old City Palace Complex', 'Jaipur', 'India', 'Experience royal Rajasthani hospitality in a restored 18th-century palace.', 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800', 4.7, 5500.00, 'WiFi,Pool,Spa,Cultural Shows,Restaurant,Heritage Tours', 'BOUTIQUE'),
('Backpacker Hub', '22 Hostel Lane, Colaba', 'Mumbai', 'India', 'Budget-friendly, clean, and social. Perfect for solo travelers and backpackers.', 'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800', 4.1, 1200.00, 'WiFi,Common Lounge,Laundry,Lockers', 'BUDGET'),
('Mountain Retreat', 'Mall Road, Upper Shimla', 'Shimla', 'India', 'Nestled in the Himalayas, offering serene mountain views and fresh air.', 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800', 4.5, 4200.00, 'WiFi,Fireplace,Restaurant,Trekking,Mountain Views', 'LUXURY');

INSERT INTO rooms (hotel_id, room_number, room_type, capacity, price_per_night, description, amenities, image_url) VALUES
-- Grand Palace
(1, '101', 'SINGLE', 1, 8500.00, 'Elegant single room with city view and premium amenities.', 'King Bed,City View,Mini Bar,Smart TV,Safe', 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800'),
(1, '201', 'DOUBLE', 2, 12000.00, 'Spacious double room with twin beds and modern decor.', 'Twin Beds,City View,Mini Bar,Smart TV,Jacuzzi', 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800'),
(1, '301', 'SUITE', 3, 25000.00, 'Presidential suite with living area, jacuzzi, and panoramic views.', 'King Bed,Living Area,Jacuzzi,Panoramic View,Butler Service', 'https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800'),
-- Sea View Resort
(2, 'B101', 'SINGLE', 1, 6500.00, 'Cozy room with direct beach access and ocean views.', 'Queen Bed,Ocean View,Balcony,Smart TV', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800'),
(2, 'B201', 'DOUBLE', 2, 9500.00, 'Spacious room with a private balcony and garden view.', 'King Bed,Garden View,Balcony,Mini Bar', 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800'),
-- City Center Inn
(3, 'C101', 'SINGLE', 1, 3500.00, 'Modern single room with high-speed WiFi for business travelers.', 'Single Bed,Workdesk,Smart TV,WiFi', 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800'),
(3, 'C201', 'DOUBLE', 2, 5500.00, 'Comfortable double room with premium bedding.', 'King Bed,Workdesk,Smart TV,Mini Fridge', 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800'),
-- Heritage Haveli
(4, 'H101', 'DELUXE', 2, 5500.00, 'Royal heritage room with traditional Rajasthani decor.', 'King Bed,Heritage Decor,Courtyard View,AC', 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=800'),
-- Backpacker Hub
(5, 'D1', 'SINGLE', 1, 1200.00, 'Clean single dorm bed with personal locker and USB charging.', 'Single Bed,Locker,USB Charging,Fan', 'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800'),
-- Mountain Retreat
(6, 'M101', 'DOUBLE', 2, 4200.00, 'Cozy mountain room with fireplace and Himalayan views.', 'King Bed,Fireplace,Mountain View,Balcony', 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800'),
(6, 'M201', 'SUITE', 4, 8000.00, 'Family suite with two bedrooms and stunning mountain panorama.', 'Two Bedrooms,Living Area,Mountain View,Fireplace,Kitchenette', 'https://images.unsplash.com/photo-1543489822-c49534f3271f?w=800');
