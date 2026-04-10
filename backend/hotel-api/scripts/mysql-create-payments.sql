-- Create payments table for local MySQL runtime (Hibernate/Flyway mixed setups).
-- Usage:
-- mysql -u root -p hotel_booking < scripts/mysql-create-payments.sql

CREATE TABLE IF NOT EXISTS payments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  booking_id CHAR(36) NOT NULL UNIQUE,
  amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(16) NOT NULL,
  method VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL,
  transaction_ref VARCHAR(128) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT payments_booking_fk FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);
