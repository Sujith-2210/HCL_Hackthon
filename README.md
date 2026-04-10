# 🏨 Hotel Booking Application

A full-stack Hotel Booking Application built with Java Spring Boot (backend) and HTML/CSS/JavaScript (frontend).

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Java 17 + Spring Boot 3.x |
| Database | MySQL / PostgreSQL |
| Auth | JWT (JSON Web Tokens) |
| Email | Spring Mail (SMTP) |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| API Testing | Postman |

---

## Project Structure

```
HCL_Hackathon/
├── backend/                    # Spring Boot application
│   ├── src/
│   │   └── main/
│   │       ├── java/com/hotel/
│   │       │   ├── config/         # Security, CORS, JWT config
│   │       │   ├── controller/     # REST controllers
│   │       │   ├── dto/            # Data Transfer Objects
│   │       │   ├── entity/         # JPA Entities
│   │       │   ├── exception/      # Global exception handling
│   │       │   ├── repository/     # Spring Data JPA repos
│   │       │   ├── service/        # Business logic
│   │       │   └── util/           # Utilities (JWT, etc.)
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Static HTML/CSS/JS
│   ├── index.html              # Landing / Search page
│   ├── login.html
│   ├── register.html
│   ├── hotels.html             # Hotel listing
│   ├── hotel-detail.html       # Hotel detail + rooms
│   ├── booking.html            # Booking form
│   ├── dashboard.html          # User dashboard / history
│   ├── css/
│   │   └── style.css
│   └── js/
│       ├── api.js              # Centralized API calls
│       ├── auth.js
│       ├── hotels.js
│       ├── booking.js
│       └── dashboard.js
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── schema.sql                  # Database schema
└── README.md
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+ or PostgreSQL 14+
- Node.js (optional, for serving frontend)
- Docker & Docker Compose

---

## Quick Start (Docker)

```bash
# Clone the repository
git clone <your-repo-url>
cd HCL_Hackathon

# Set up environment variables
cp backend/.env.example backend/.env
# Edit .env with your database credentials and SMTP settings

# Start all services
docker-compose up -d

# Access the app
# Frontend: http://localhost:8080
# API:      http://localhost:8080/api
```

---

## Manual Setup

### 1. Database

```sql
CREATE DATABASE hotel_booking;
-- Then run schema.sql
mysql -u root -p hotel_booking < schema.sql
```

### 2. Backend

```bash
cd backend

# Configure application.yml or set environment variables
# Run the application
mvn spring-boot:run
```

Backend starts at: `http://localhost:8080`

### 3. Frontend

Open `frontend/index.html` in a browser, or serve with any static file server:

```bash
cd frontend
npx serve .
```

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |
| POST | `/api/auth/logout` | Logout |

### Hotels
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hotels` | Search/list hotels |
| GET | `/api/hotels/{id}` | Get hotel details |
| GET | `/api/hotels/{id}/rooms` | Get available rooms |

### Bookings
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Create booking |
| GET | `/api/bookings/my` | Get my bookings |
| GET | `/api/bookings/{id}` | Get booking by ID |
| DELETE | `/api/bookings/{id}` | Cancel booking |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Get user profile |
| PUT | `/api/users/profile` | Update profile |

---

## Environment Variables

```env
DB_URL=jdbc:mysql://localhost:3306/hotel_booking
DB_USERNAME=root
DB_PASSWORD=yourpassword
JWT_SECRET=your-256-bit-secret
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your-app-password
```

---

## Database Schema

See [`schema.sql`](./schema.sql) for the complete database schema.

---

## GitHub Actions CI/CD

The pipeline (`.github/workflows/ci-cd.yml`) runs on every push:
1. Build Maven project
2. Run unit tests
3. Build Docker image
4. Push to Docker Hub

---

## Team

Each member should commit code individually to the repository.

---

## License

MIT
