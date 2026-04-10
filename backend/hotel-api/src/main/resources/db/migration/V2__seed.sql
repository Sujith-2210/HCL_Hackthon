INSERT INTO amenities (id, name) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Free WiFi'),
  ('22222222-2222-2222-2222-222222222222', 'Pool'),
  ('33333333-3333-3333-3333-333333333333', 'Breakfast');

INSERT INTO hotels (id, name, description, address, city, country, star_rating) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'HCL Grand', 'Business-friendly stay near downtown.', '123 Main St', 'Chennai', 'India', 4),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Coastal Retreat', 'Beachside hotel with calm vibes.', '77 Ocean Rd', 'Chennai', 'India', 5);

INSERT INTO hotel_amenities (hotel_id, amenity_id) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333');

INSERT INTO room_types (id, hotel_id, name, description, max_occupancy, base_price_per_night, currency) VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Standard', 'Comfortable room for work trips.', 2, 4500.00, 'INR'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Deluxe', 'Bigger room with better view.', 3, 6500.00, 'INR'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Sea View', 'Sea-facing room with balcony.', 2, 9000.00, 'INR');

INSERT INTO room_inventory (id, room_type_id, room_label, is_active) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'STD-101', true),
  ('f0000000-0000-0000-0000-000000000002', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'STD-102', true),
  ('f0000000-0000-0000-0000-000000000003', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'DLX-201', true),
  ('f0000000-0000-0000-0000-000000000004', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SEA-301', true),
  ('f0000000-0000-0000-0000-000000000005', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'SEA-302', true);

-- Demo users (password is "Password@123" hashed with BCrypt; you can re-generate later)
INSERT INTO users (id, email, password_hash, full_name, role) VALUES
  ('99999999-9999-9999-9999-999999999991', 'admin@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Admin User', 'ADMIN'),
  ('99999999-9999-9999-9999-999999999992', 'reception@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Reception User', 'RECEPTIONIST'),
  ('99999999-9999-9999-9999-999999999993', 'customer@hotel.test', '$2a$10$4.kK6Krg8OFvktKIOFmsJ./OCKV7uHv1Rx6eQ.xWL53x5JlUwk2Ju', 'Customer User', 'CUSTOMER');

-- Real-world availability example:
-- SEA-301 is booked only for (day after tomorrow) and the next day; today it's still available for other dates.
INSERT INTO bookings (id, user_id, room_inventory_id, check_in, check_out, guest_count, status, total_amount, currency)
VALUES
  ('77777777-7777-7777-7777-777777777777',
   '99999999-9999-9999-9999-999999999993',
   'f0000000-0000-0000-0000-000000000004',
   (CURRENT_DATE + 2),
   (CURRENT_DATE + 4),
   2,
   'CONFIRMED',
   18000.00,
   'INR');
