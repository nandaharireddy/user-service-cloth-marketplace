# User Service — Clothing Marketplace

The **User Service** is the identity and authentication authority for the Clothing Marketplace platform. It is responsible for user registration, credential verification, JWT token issuance (`ROLE_CUSTOMER` and `ROLE_VENDOR`), revocable server-side refresh tokens, and customer shipping address management.

---

## 1. Responsibilities

- **Authentication & Authorization**: Registration, login, token refresh, and logout with BCrypt password hashing and HS256 JWT tokens.
- **Address Management**: Full CRUD operations for customer shipping addresses with default address handling.
- **Security & Rate Limiting**: In-memory sliding window rate limiting on sensitive `/auth/login` and `/auth/register` endpoints.
- **Service Discovery**: Registers as a Eureka client under `user-service`.
- **API Documentation**: Interactive Swagger UI docs at `/swagger-ui.html`.

---

## 2. Architecture: 4-Layer Pattern

Conforms strictly to `GEMINI.md`:
```
Controller  →  Service  →  DAO  →  Repository
```
- **Controller** (`com.clothmarket.user.controller`): HTTP routing, validation (`@Valid`), OpenAPI documentation.
- **Service** (`com.clothmarket.user.service`): Business logic, transaction boundaries (`@Transactional`).
- **DAO** (`com.clothmarket.user.dao`): Composes and executes repository calls.
- **Repository** (`com.clothmarket.user.repository`): Spring Data JPA repository query declarations.

---

## 3. Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/register` | Register customer/vendor | No |
| `POST` | `/auth/login` | Login and receive JWT access + refresh tokens | No |
| `POST` | `/auth/refresh` | Exchange refresh token for new access token | No |
| `POST` | `/auth/logout` | Invalidate refresh token server-side | Optional |
| `GET` | `/addresses` | List current user's addresses (paginated) | Yes (Bearer) |
| `GET` | `/addresses/{id}` | Get single address by ID | Yes (Bearer) |
| `POST` | `/addresses` | Add new shipping address | Yes (Bearer) |
| `PATCH` | `/addresses/{id}` | Edit shipping address | Yes (Bearer) |
| `DELETE` | `/addresses/{id}` | Delete shipping address | Yes (Bearer) |

---

## 4. Running Locally

### Prerequisites
- Java 21 (JDK 21)
- PostgreSQL running locally with database `cloth_marketplace` and schema `user_service`
- Eureka Server running on `http://localhost:8761`

### Environment Setup
Copy `.env.example` to `.env` or set environment variables:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/cloth_marketplace?currentSchema=user_service
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=clothmarketplace-super-secret-jwt-key-must-be-at-least-256-bits-long-for-hs256-algorithm
```

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```
