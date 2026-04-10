CREATE TABLE users (
  id UUID PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  full_name TEXT NOT NULL,
  role TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE hotels (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  address TEXT NOT NULL,
  city TEXT NOT NULL,
  country TEXT NOT NULL,
  star_rating INTEGER NOT NULL DEFAULT 3,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX hotels_city_idx ON hotels(city);

CREATE TABLE amenities (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE hotel_amenities (
  hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
  amenity_id UUID NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
  PRIMARY KEY (hotel_id, amenity_id)
);

CREATE TABLE room_types (
  id UUID PRIMARY KEY,
  hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  max_occupancy INTEGER NOT NULL,
  base_price_per_night NUMERIC(12, 2) NOT NULL,
  currency TEXT NOT NULL DEFAULT 'USD'
);

CREATE INDEX room_types_hotel_id_idx ON room_types(hotel_id);

CREATE TABLE room_inventory (
  id UUID PRIMARY KEY,
  room_type_id UUID NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
  room_label TEXT NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX room_inventory_room_type_id_idx ON room_inventory(room_type_id);

CREATE TABLE bookings (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  room_inventory_id UUID NOT NULL REFERENCES room_inventory(id),
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  guest_count INTEGER NOT NULL,
  status TEXT NOT NULL,
  total_amount NUMERIC(12, 2) NOT NULL,
  currency TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX bookings_user_id_idx ON bookings(user_id);
CREATE INDEX bookings_room_inventory_id_idx ON bookings(room_inventory_id);
CREATE INDEX bookings_date_range_idx ON bookings(check_in, check_out);

ALTER TABLE bookings
  ADD CONSTRAINT bookings_check_dates CHECK (check_out > check_in);

