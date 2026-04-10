-- Initialize local MySQL demo schema and baseline seed data.
-- Safe to run multiple times: tables are created if missing and seed rows use INSERT IGNORE.
-- Usage:
-- mysql -u root -p hotel_booking < scripts/mysql-init-demo.sql

SET @ts = UTC_TIMESTAMP(3);

CREATE TABLE IF NOT EXISTS users (
  id CHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS hotels (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  address VARCHAR(255) NOT NULL,
  city VARCHAR(128) NOT NULL,
  country VARCHAR(128) NOT NULL,
  star_rating INT NOT NULL DEFAULT 3,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY hotels_city_idx (city)
);

CREATE TABLE IF NOT EXISTS amenities (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS hotel_amenities (
  hotel_id CHAR(36) NOT NULL,
  amenity_id CHAR(36) NOT NULL,
  PRIMARY KEY (hotel_id, amenity_id),
  CONSTRAINT hotel_amenities_hotel_fk FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
  CONSTRAINT hotel_amenities_amenity_fk FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS room_types (
  id CHAR(36) NOT NULL PRIMARY KEY,
  hotel_id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  max_occupancy INT NOT NULL,
  base_price_per_night DECIMAL(12,2) NOT NULL,
  currency VARCHAR(16) NOT NULL DEFAULT 'INR',
  CONSTRAINT room_types_hotel_fk FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
  KEY room_types_hotel_id_idx (hotel_id)
);

CREATE TABLE IF NOT EXISTS room_inventory (
  id CHAR(36) NOT NULL PRIMARY KEY,
  room_type_id CHAR(36) NOT NULL,
  room_label VARCHAR(128) NOT NULL,
  is_active BIT(1) NOT NULL DEFAULT b'1',
  CONSTRAINT room_inventory_room_type_fk FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,
  KEY room_inventory_room_type_id_idx (room_type_id)
);

CREATE TABLE IF NOT EXISTS bookings (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  room_inventory_id CHAR(36) NOT NULL,
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  guest_count INT NOT NULL,
  status VARCHAR(32) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT bookings_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT bookings_room_inventory_fk FOREIGN KEY (room_inventory_id) REFERENCES room_inventory(id),
  CONSTRAINT bookings_check_dates CHECK (check_out > check_in),
  KEY bookings_user_id_idx (user_id),
  KEY bookings_room_inventory_id_idx (room_inventory_id),
  KEY bookings_date_range_idx (check_in, check_out)
);

CREATE TABLE IF NOT EXISTS payments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  booking_id CHAR(36) NOT NULL UNIQUE,
  amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  method VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL,
  transaction_ref VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT payments_booking_fk FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  KEY payments_booking_id_idx (booking_id)
);

INSERT IGNORE INTO amenities (id, name) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Free WiFi'),
  ('22222222-2222-2222-2222-222222222222', 'Pool'),
  ('33333333-3333-3333-3333-333333333333', 'Breakfast');

INSERT IGNORE INTO hotels (id, name, description, address, city, country, star_rating, created_at) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'HCL Grand', 'Business-friendly stay near downtown.', '123 Main St', 'Chennai', 'India', 4, @ts),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Coastal Retreat', 'Beachside hotel with calm vibes.', '77 Ocean Rd', 'Chennai', 'India', 5, @ts);

INSERT IGNORE INTO hotel_amenities (hotel_id, amenity_id) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333');

INSERT IGNORE INTO room_types (id, hotel_id, name, description, max_occupancy, base_price_per_night, currency) VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Standard', 'Comfortable room for work trips.', 2, 4500.00, 'INR'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Deluxe', 'Bigger room with better view.', 3, 6500.00, 'INR'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Sea View', 'Sea-facing room with balcony.', 2, 9000.00, 'INR');

INSERT IGNORE INTO room_inventory (id, room_type_id, room_label, is_active) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'STD-101', b'1'),
  ('f0000000-0000-0000-0000-000000000002', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'STD-102', b'1'),
  ('f0000000-0000-0000-0000-000000000003', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'DLX-201', b'1'),
  ('f0000000-0000-0000-0000-000000000004', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SEA-301', b'1'),
  ('f0000000-0000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SEA-302', b'1');

INSERT IGNORE INTO users (id, email, password_hash, full_name, role, created_at) VALUES
  ('99999999-9999-9999-9999-999999999991', 'admin@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Admin User', 'ADMIN', @ts),
  ('99999999-9999-9999-9999-999999999992', 'reception@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Reception User', 'RECEPTIONIST', @ts),
  ('99999999-9999-9999-9999-999999999993', 'customer@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Customer User', 'CUSTOMER', @ts);

INSERT IGNORE INTO bookings (id, user_id, room_inventory_id, check_in, check_out, guest_count, status, total_amount, currency, created_at, updated_at) VALUES
  ('77777777-7777-7777-7777-777777777777',
   '99999999-9999-9999-9999-999999999993',
   'f0000000-0000-0000-0000-000000000004',
   DATE_ADD(CURDATE(), INTERVAL 2 DAY),
   DATE_ADD(CURDATE(), INTERVAL 4 DAY),
   2,
   'CONFIRMED',
   18000.00,
   'INR',
   @ts,
   @ts);
