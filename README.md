# Order Service (OLX Backend Engineer Intern Technical Test)

[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.0](https://img.shields.io/badge/Spring_Boot-4.0.7-green?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-darkgreen?logo=gradel&logoColor=white)](https://gradle.org/)
[![H2 Database](https://img.shields.io/badge/Database-H2-orange?logo=h2&logoColor=white)](https://www.h2database.com/)

Spring Boot backend for a simple ecommerce shop. Handles order CRUD, status lifecycle, and sorting. Built with Vertical Slice Architecture and Domain Driven Design.

---

## Features

- **Full CRUD.** Create, read (single and list), update, and delete. Data persists across restarts via file based H2.
- **RESTful API.** Resource oriented URLs, proper HTTP methods, consistent status codes, input validation, descriptive error responses.
- **Order Lifecycle.** State machine based: *CREATED*, *PAID*, *SHIPPED*, *DELIVERED*, *CANCELLED*. Illegal moves return 409 Conflict.
- **Server Computed Total.** Total is calculated from line items server side. Client cant set it.
- **Extensible Sorting.** List endpoint supports newest, highest total, oldest unpaid. Adding a sort rule means one new class and one registration. No existing code changes.
- **Immutable Items After Payment.** Once status hits *PAID*, line items are locked.
- **Unit Tested.** Business logic, validation, transitions, and error paths all covered. Run with one command.
- **Internally Modular.** Domain, application, and infrastructure are separated within the order slice.

---

## Versions

| Component   | Version |
|-------------|---------|
| Java        | 21 LTS  |
| Spring Boot | 4.0.7   |
| Gradle      | 8.x     |

---

## Quick Start

**Prerequisites**
- JDK 21 LTS ([Temurin](https://adoptium.net/temurin/releases?version=21&os=any&arch=any) Recommended)
- IntelliJ IDEA, Visual Studio Code, or any IDE

### Step 1: Build the project

```bash
./gradlew build
```

Or with the Makefile:

```bash
make build
```

### Step 2: Run the service

```bash
./gradlew bootRun
```

Or:

```bash
make run
```

The service starts on http://localhost:8080.

### Step 3: Create an order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Andi Wijaya",
    "items": [
      { "productName": "Apple", "quantity": 3, "unitPrice": 0.50 },
      { "productName": "Bread Loaf", "quantity": 1, "unitPrice": 2.20 }
    ]
  }'
```

### Step 4: List orders

```bash
curl "http://localhost:8080/api/orders?page=0&size=20&sort=newest"
```

### Step 5: Run tests

```bash
./gradlew test
```

---

## Architecture

### Project Structure

```
aif-olx-technical-test/
├── src/
│   ├── main/
│   │   ├── java/com/olx/orderservice/
│   │   │   ├── OrderServiceApplication.java
│   │   │   ├── config/
│   │   │   │   └── JacksonConfig.java
│   │   │   ├── order/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Order.java
│   │   │   │   │   │   ├── OrderStatus.java
│   │   │   │   │   │   └── LineItem.java
│   │   │   │   │   ├── service/
│   │   │   │   │   │   └── OrderDomainService.java
│   │   │   │   │   └── port/
│   │   │   │   │       └── OrderRepository.java
│   │   │   │   ├── application/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── OrderApplicationService.java
│   │   │   │   │   │   └── OrderMapper.java
│   │   │   │   │   └── exception/
│   │   │   │   └── infrastructure/
│   │   │   │       ├── persistence/
│   │   │   │       └── web/
│   │   │   │           ├── OrderController.java
│   │   │   │           └── GlobalExceptionHandler.java
│   │   │   └── security/
│   │   │       ├── ForbiddenFieldsFilter.java
│   │   │       └── BufferedServletWrapper.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/olx/orderservice/
│           ├── order/
│           └── security/
│               └── ForbiddenFieldsFilterTest.java
├── api-collection/
│   ├── postman/
│   └── bruno/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### Layer Responsibilities

- **Domain Layer.** Pure Java, zero framework imports. Order aggregate root with behavior methods, OrderStatus with transition validation, LineItem value object, OrderDomainService for rules, and the OrderRepository port interface.

- **Application Layer.** Use cases live here. OrderApplicationService coordinates operations, DTOs shape request and response data with Jakarta Validation, OrderMapper converts between domain and DTO.

- **Infrastructure Layer.** Spring Data JPA persistence, REST endpoints via OrderController, global error handling via GlobalExceptionHandler, and the ForbiddenFieldsFilter for security.

---

## API Reference

API collections for both **Postman** and **Bruno** are available in `api-collection/`.

### Postman

Import `api-collection/postman/Order Service.postman_collection.json` into Postman. The environment file `api-collection/postman/Order Service Environment.postman_environment.json` is optional — the collection auto-sets `baseUrl` to `http://localhost:8080` and extracts `orderId` from the Create Order response.

Run the entire collection (File → Run Collection) — all 17 requests execute in sequence with zero manual config.

### Bruno

```
brew install bruno-cli        # if not already installed
cd api-collection/bruno
bru run -r --env-file env/env.json
```

All 17 requests run recursively with assertions validating every endpoint. Environment variables (`baseUrl`, `orderId`, `mainOrderId`) are pre-configured in `env/env.json`.

### Create Order

```
POST /api/orders
```

Request Body:

```json
{
  "customerName": "Andi Wijaya",
  "items": [
    { "productName": "Apple", "quantity": 3, "unitPrice": 0.50 },
    { "productName": "Bread Loaf", "quantity": 1, "unitPrice": 2.20 }
  ]
}
```

Response: 201 Created

```json
{
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerName": "Andi Wijaya",
  "items": [
    { "productName": "Apple", "quantity": 3, "unitPrice": 0.50 },
    { "productName": "Bread Loaf", "quantity": 1, "unitPrice": 2.20 }
  ],
  "status": "CREATED",
  "totalAmount": 3.70,
  "createdAt": "2026-07-08T10:30:00Z",
  "updatedAt": "2026-07-08T10:30:00Z"
}
```

### Get Order

```
GET /api/orders/{orderId}
```

Response: 200 OK with order body
Response: 404 Not Found for unknown ID

### List Orders

```
GET /api/orders?page=0&size=20&sort=newest
```

Response: 200 OK

```json
{
  "orders": [
    { "...": "..." }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Supported sort values: newest, highest_total, oldest_unpaid.

### Update Order

```
PUT /api/orders/{orderId}
```

Updates customerName and items. Line items cannot be modified if status is *PAID* or beyond. Returns 400 Bad Request if modification is not allowed.

### Cancel Order

```
PATCH /api/orders/{orderId}/status
```

Request Body:

```json
{
  "status": "CANCELLED",
  "cancellationReason": "Customer requested cancellation"
}
```

Legal transitions to *CANCELLED*: from *CREATED*, *PAID*, or *SHIPPED*. Cancellation requires a reason.

### Pay Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "PAID"
}
```

Legal transition: from *CREATED*.

### Ship Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "SHIPPED"
}
```

Legal transition: from *PAID*.

### Deliver Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "DELIVERED"
}
```

Legal transition: from *SHIPPED*.

### Delete Order

```
DELETE /api/orders/{orderId}
```

Response: 204 No Content
Response: 404 Not Found

### Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Customer name must not be blank",
  "timestamp": "2026-07-08T10:30:00Z",
  "path": "/api/orders"
}
```

HTTP status codes used: 201 Created, 200 OK, 204 No Content, 400 Bad Request, 404 Not Found, 409 Conflict.

---

## Configuration

Configuration is in `src/main/resources/application.yml`.

| Property                      | Default                    | Description                 |
|-------------------------------|----------------------------|-----------------------------|
| server.port                   | 8080                       | HTTP port                   |
| spring.datasource.url         | jdbc:h2:file:./data/orders | H2 file based database path |
| spring.jpa.hibernate.ddl-auto | update                     | Schema generation strategy  |

---

## Design Decisions

**Vertical Slice Architecture**

Code is organized by feature (order) instead of by technical layer. Each slice holds its own domain, application, and infrastructure packages. This keeps things navigable and isolated. When a new feature comes in, it lives in its own slice without touching unrelated code.

**Total Amount is Server Computed**

The client never sends totalAmount. The server sums up quantity times unitPrice across all items using BigDecimal with scale 2 and HALF_UP rounding. If a client somehow includes totalAmount, its silently ignored.

**Currency Scope**

All money values use a single assumed currency. BigDecimal with scale 2 handles the math. Currency conversion is out of scope.

**H2 Database**

H2 runs in file mode so data sticks around between restarts. No external database needed. The file lives at ./data/orders.

**UUID Generation**

The server generates orderId as UUID v4. Anything the client sends for orderId in a create request gets ignored. Path variable orderId is the only source of truth for updates and deletes.

**Hard Delete**

DELETE actually removes the row. Soft delete would add complexity with no real benefit here.

**Cancel vs Delete**

Cancel changes the order status to *CANCELLED* and requires a reason. Delete wipes the record entirely. Two different operations for two different needs.

**Strategy Pattern for Sorting**

Each sort option is its own class implementing the same interface. A registry maps keys like newest or highest_total to the right implementation. Adding a new sort means writing one class and registering it. Thats it.

**State Machine for Status Transitions**

OrderStatus has a transition matrix baked in. The Order class checks this matrix every time a status change is requested. Illegal moves are caught before they can corrupt state.

**Separate JPA Entities from Domain Model**

JPA annotations live on separate entity classes, not on the domain model. This keeps the domain layer pure Java with zero framework dependencies.

**Part 2 Influence on Part 1 Design**

Part 2 requirements were known before Part 1 was built. The state machine was there from the start. Cancellation reason was part of the domain model. Item lock checks were placed in the domain layer. Sorting was abstracted behind an interface from day one. When Part 2 came around, nothing had to be rewritten.

**Scope Deliberately Omitted**

Authentication and authorization, Swagger documentation, testcontainers integration tests, audit logging, event sourcing, and CQRS were left out to keep the scope focused on what the assessment asks.

**Future Improvements**

- Spring Security for role based access control
- OpenAPI / Swagger for API docs
- Integration tests with testcontainers
- Audit logging for status changes
- Idempotency keys for create requests
- Rate limiting
- Metrics and health checks via Spring Boot Actuator

---

## Security Extension

### Hostile Input Detection

A servlet filter (ForbiddenFieldsFilter) checks POST, PUT, and PATCH requests going to `/api/orders`. It looks for JSON keys that should never come from a client:

- `orderId` (server generates this)
- `status` (the state machine manages this)
- `totalAmount` (server computes this from line items)

If any of these show up in the request body, the filter returns 400 Bad Request right away. The regex only matches JSON property key syntax, so string values containing these words wont trigger false alarms. The `/status` endpoint is excluded since *status* is a legitimate field there.

### Information Disclosure

All errors go through a standard ErrorResponse DTO. It exposes status code, error type, message, timestamp, and request path. Stack traces, database errors, or framework internals never leak out. The GlobalExceptionHandler catches everything and maps it to safe responses. Unexpected errors return a generic message.

### Access Control

Authentication and authorization are out of scope. The service assumes trusted clients on a protected network. A production setup would add Spring Security with JWT or OAuth2.

---

## Deployment Extension

The service ships as a Docker image. Two commands get it running anywhere Docker is installed.

### Multi Stage Build

The Dockerfile has two stages:

1. Build. Gradle image compiles the app. Dependencies download before source code copies over, so Docker layer caching kicks in on rebuilds.
2. Runtime. Minimal JRE image runs the jar. Nothing but the built artifact makes it into the final image.

### Hardening

- Runs as `appuser`, not root
- Uses `/dev/./urandom` for secure random
- Cleans apt lists after installing curl
- `.dockerignore` keeps dev files out of the build context
- HEALTHCHECK polls the API every 30 seconds

### One Command

```bash
docker compose up --build
```

That builds the image, starts the container, maps port 8080, and mounts a volume for the H2 database file.

---

## Submission

This project is submitted as part of the technical test for the Backend Engineer Intern position at PT Astra Digital Mobil through the Astra Internship Fair (AIF) 2026.

| Identity  | Detail                       |
|-----------|------------------------------|
| Applicant | Raihan Putra Kirana          |
| Position  | Backend Engineer Intern      |
| Company   | PT Astra Digital Mobil (OLX) |

---