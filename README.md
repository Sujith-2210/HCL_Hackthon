## Hotel Booking Application

Production-ready hackathon stack with **HTML/CSS/JS** frontend + **Java Spring Boot** REST API.

### Features

- **Search hotels** by city and dates (availability uses real date-range overlap)
- **View hotel details** with room types and amenities
- **Auth**: register + login (JWT)
- **Roles**: `CUSTOMER`, `RECEPTIONIST`, `ADMIN`
- **Bookings**: create, cancel, history
- **Migrations + seed data** via Flyway

### Tech

- **Frontend**: `frontend/` (static)
- **Backend**: `backend/hotel-api/` (Spring Boot)
- **Database**: MySQL (local baseline), PostgreSQL (Flyway path)
- **Tools**: Docker, Postman, Git

### Quick start (Docker)

Copy env file:

```bash
cp .env.example .env
```

Start everything:

```bash
docker compose up --build
```

Open:

- **Frontend**: `http://localhost:3000`
- **API**: `http://localhost:8080`

### Demo users (seed)

Password for all: `Password@123`

- `admin@hotel.test` (ADMIN)
- `reception@hotel.test` (RECEPTIONIST)
- `customer@hotel.test` (CUSTOMER)

### API endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register customer |
| `POST` | `/api/auth/login` | Public | Login and get JWT |
| `GET` | `/api/hotels?city=&checkIn=&checkOut=&guests=` | Public | Search hotels |
| `GET` | `/api/hotels/{hotelId}` | Public | Hotel details |
| `GET` | `/api/hotels/{hotelId}/rooms` | Public | Room options |
| `GET` | `/api/users/profile` | CUSTOMER / RECEPTIONIST / ADMIN | View own profile |
| `POST` | `/api/bookings` | CUSTOMER | Create booking |
| `GET` | `/api/bookings` | CUSTOMER | Customer booking history |
| `GET` | `/api/bookings/my` | CUSTOMER | Customer booking history (alias used by frontend) |
| `PATCH` | `/api/bookings/{bookingId}/cancel` | CUSTOMER | Cancel own booking |
| `DELETE` | `/api/bookings/{bookingId}` | CUSTOMER | Cancel own booking (alias) |
| `POST` | `/api/payments` | CUSTOMER | Make payment for booking |
| `GET` | `/api/payments/booking/{bookingId}` | CUSTOMER / RECEPTIONIST / ADMIN | View payment by booking |
| `GET` | `/api/bookings/all` | RECEPTIONIST / ADMIN | View all bookings |
| `PATCH` | `/api/bookings/{bookingId}/confirm` | RECEPTIONIST | Confirm booking |
| `PATCH` | `/api/bookings/{bookingId}/check-in` | RECEPTIONIST | Check-in guest |
| `PATCH` | `/api/bookings/{bookingId}/check-out` | RECEPTIONIST | Check-out guest |
| `PATCH` | `/api/bookings/{bookingId}/complete` | RECEPTIONIST | Complete stay |
| `GET` | `/api/admin/users` | ADMIN | List users |
| `PATCH` | `/api/admin/users/{userId}/role` | ADMIN | Update user role |
| `DELETE` | `/api/admin/users/{userId}` | ADMIN | Delete user |
| `GET` | `/api/admin/hotels` | ADMIN | List hotels |
| `POST` | `/api/admin/hotels` | ADMIN | Create hotel |
| `PATCH` | `/api/admin/hotels/{hotelId}` | ADMIN | Update hotel |
| `DELETE` | `/api/admin/hotels/{hotelId}` | ADMIN | Delete hotel |
| `GET` | `/api/admin/rooms` | ADMIN | List rooms |
| `POST` | `/api/admin/rooms` | ADMIN | Create room type |
| `PATCH` | `/api/admin/rooms/{roomId}` | ADMIN | Update room type |
| `DELETE` | `/api/admin/rooms/{roomId}` | ADMIN | Delete room type |
| `GET` | `/api/admin/reports` | ADMIN | System summary metrics |

### Local MySQL run (recommended baseline)

Single command (recommended):

```bash
./run.sh
```

Load extra seed data while starting:

```bash
./run.sh --seed
```

Reset the local MySQL demo DB back to a clean baseline before starting:

```bash
./run.sh --reset
```

Reset and load richer demo data in one go:

```bash
./run.sh --reset --seed
```

This starts backend + frontend together and auto-runs payments schema setup.

Use project env file first:

```bash
cp .env.example .env
```

Then start backend with local MySQL (all secrets/config are read from `.env`):

```bash
cd backend/hotel-api
set -a
source ../../.env
set +a
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/hotel_booking?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-root}" \
SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-sujith@2204$}" \
SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:-false}" \
SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO:-update}" \
./mvnw spring-boot:run
```

Create payments table (required when Flyway is disabled):

```bash
MYSQL_PWD="${SPRING_DATASOURCE_PASSWORD:-sujith@2204$}" mysql -u "${SPRING_DATASOURCE_USERNAME:-root}" hotel_booking < scripts/mysql-create-payments.sql
```

Optional richer seed data:

```bash
MYSQL_PWD="${SPRING_DATASOURCE_PASSWORD:-sujith@2204$}" mysql -u "${SPRING_DATASOURCE_USERNAME:-root}" hotel_booking < scripts/mysql-seed-more-data.sql
```

### SerpAPI behavior

- Hotel search uses SerpAPI first when `APP_SERPAPI_KEY` is set.
- India defaults are applied (`gl=in`, `currency=INR`) unless overridden by env vars.
- If SerpAPI is unavailable/empty, backend falls back to local DB hotels.
- Details and rooms support both local UUID hotel IDs and Serp IDs (`serp::<property_token>`).
- Booking is enabled only for local DB inventory rooms.

### End-to-end smoke checklist

1. Register a customer and login.
2. Verify dashboard loads bookings and profile.
3. Search hotels from homepage/hotels page.
4. Open hotel details and rooms.
5. Book a local DB hotel room.
6. Cancel the booking from dashboard.
7. Create payment and fetch payment by booking ID.

### Availability logic (real-world)

For requested \([checkIn, checkOut)\), a room is considered booked if there exists a `CONFIRMED` booking with:

\[
existing.checkIn < requested.checkOut \;\; \text{AND} \;\; existing.checkOut > requested.checkIn
\]

So a room can be unavailable for future days but still available today for other date ranges.
