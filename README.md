# Order Service (OLX Backend Engineer Technical Test)

[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.0](https://img.shields.io/badge/Spring_Boot-4.0.7-green?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-darkgreen?logo=gradel&logoColor=white)](https://gradle.org/)
[![H2 Database](https://img.shields.io/badge/Database-H2-orange?logo=h2&logoColor=white)](https://www.h2database.com/)

Spring Boot backend for a simple ecommerce shop implementing an Order Service with full CRUD, RESTful API, order lifecycle management, and extensible sorting. Built with Vertical Slice Architecture and Domain Driven Design.

---

## Features

- **Full CRUD:** Create, read (single and list), update, and delete orders. Data persists across requests using file based H2.
- **RESTful API Design:** Resource oriented URLs, correct HTTP methods, consistent status codes, input validation, and descriptive error responses.
- **Order Lifecycle Management:** Status transitions follow a state machine: CREATED, PAID, SHIPPED, DELIVERED, CANCELLED. Illegal transitions are rejected with 409 Conflict.
- **Server Computed Total:** The order total is calculated from line items on the server. Client supplied total is ignored.
- **Extensible Sorting:** The list endpoint supports multiple ordering rules: newest, highest total, oldest unpaid. New rules can be added without modifying existing code using the Strategy pattern.
- **Immutable Items After Payment:** Once an order reaches PAID status, its line items are locked from modification.
- **Unit Tested:** Business logic, validation, status transitions, and error paths are covered by unit tests. Run with a single command.
- **Internally Modular:** Domain, application, and infrastructure layers are separated within the order vertical slice.

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
- IntelliJ IDEA

### Step 1: Build the project

```bash
./gradlew build
```

### Step 2: Run the service

```bash
./gradlew bootRun
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
📦 aif-olx-technical-test/
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/aif/orderservice/
│   │   │   ├── 📄 OrderServiceApplication.java
│   │   │   ├── 📁 config/
│   │   │   │   └── 📄 JacksonConfig.java
│   │   │   ├── 📁 order/
│   │   │   │   ├── 📁 domain/
│   │   │   │   │   ├── 📁 model/
│   │   │   │   │   │   ├── 📄 Order.java
│   │   │   │   │   │   ├── 📄 OrderStatus.java
│   │   │   │   │   │   └── 📄 LineItem.java
│   │   │   │   │   ├── 📁 service/
│   │   │   │   │   │   └── 📄 OrderDomainService.java
│   │   │   │   │   └── 📁 port/
│   │   │   │   │       └── 📄 OrderRepository.java
│   │   │   │   ├── 📁 application/
│   │   │   │   │   ├── 📁 dto/
│   │   │   │   │   ├── 📁 service/
│   │   │   │   │   │   ├── 📄 OrderApplicationService.java
│   │   │   │   │   │   └── 📄 OrderMapper.java
│   │   │   │   │   └── 📁 exception/
│   │   │   │   └── 📁 infrastructure/
│   │   │   │       ├── 📁 persistence/
│   │   │   │       └── 📁 web/
│   │   │   │           ├── 📄 OrderController.java
│   │   │   │           └── 📄 GlobalExceptionHandler.java
│   │   │   └── 📁 security/
│   │   │       ├── 📄 ForbiddenFieldsFilter.java
│   │   │       └── 📄 BufferedServletWrapper.java
│   │   └── 📁 resources/
│   │       └── 📄 application.yml
│   └── 📁 test/
│       └── 📁 java/com/olx/orderservice/
│           ├── 📁 order/
│           └── 📁 security/
│               └── 📄 ForbiddenFieldsFilterTest.java
├── 📄 build.gradle.kts
├── 📄 settings.gradle.kts
├── 📄 Dockerfile
├── 📄 docker-compose.yml
├── 📄 README.md
└── 📄 PLAN.md
```

### Layer Responsibilities

- **Domain Layer:** Pure Java with no framework imports. Contains the Order aggregate root with behavior methods, OrderStatus enum with transition validation, LineItem value object with self validation, OrderDomainService for complex domain rules, and the OrderRepository port interface.

- **Application Layer:** Orchestrates use cases through OrderApplicationService, defines request and response DTOs with Jakarta Validation annotations, handles domain to DTO mapping, and defines domain specific exceptions.

- **Infrastructure Layer:** Implements persistence with Spring Data JPA (JpaOrderRepository, JPA entities separate from domain model), exposes REST endpoints through OrderController, and handles exceptions globally through GlobalExceptionHandler with consistent error response formatting.

---

## API Reference

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

Updates customerName and items. Line items cannot be modified if status is PAID or beyond. Returns 400 Bad Request if modification is not allowed.

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

Legal transitions to CANCELLED: from CREATED, PAID, or SHIPPED. Cancellation requires a reason.

### Pay Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "PAID"
}
```

Legal transition: from CREATED.

### Ship Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "SHIPPED"
}
```

Legal transition: from PAID.

### Deliver Order

```
PATCH /api/orders/{orderId}/status
```

```json
{
  "status": "DELIVERED"
}
```

Legal transition: from SHIPPED.

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

## Build and Run

### Build the project

```bash
./gradlew build
```

### Run tests

```bash
./gradlew test
```

### Run the service

```bash
./gradlew bootRun
```

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

- **Total Amount is Server Computed.** The client cannot supply totalAmount. The server calculates it as the sum of quantity multiplied by unitPrice for each line item using BigDecimal with scale 2 and HALF_UP rounding. This prevents inconsistencies between line items and the total. If a client provides totalAmount, it is ignored.
- **Currency Scope.** All monetary values are in a single assumed currency. Currency conversion is out of scope for this assessment. The BigDecimal type with scale 2 is used for all monetary amounts.
- **H2 Database.** H2 in file mode persists data across restarts without requiring an external database installation. This satisfies the requirement that data must persist across requests. The database file is stored in ./data/orders.
- **UUID Generation.** The server generates orderId as UUID Version 4. Any orderId supplied by the client in a create request is ignored. In update and delete operations, the path variable orderId is the source of truth.
- **Hard Delete.** DELETE permanently removes the order from storage. Soft delete was not required and would add unnecessary complexity.
- **Cancel versus Delete.** Cancel transitions the order status to CANCELLED with a required reason. Delete removes the record permanently. They are separate operations with different semantics.
- **Strategy Pattern for Sorting.** Sorting rules are implemented using the Strategy pattern. Each sort option is a separate implementation class implementing a common strategy interface. Adding a new sort rule requires creating a new class and registering it in the strategy registry. No existing sort code is modified.
- **State Machine for Status Transitions.** The OrderStatus enum contains a transition matrix that defines all legal transitions. The domain model enforces these rules at the behavior level through methods like canTransitionTo. This prevents illegal states from being reached through any code path.
- **Separate JPA Entities from Domain Model.** JPA entities (OrderEntity, OrderLineItemEntity) are separate classes from the domain model (Order, LineItem). This prevents JPA annotations from leaking into the domain layer and allows the domain model to remain pure Java with framework annotations.
- **Part 2 Influence on Part 1 Design.** The constrained status transitions, cancellation reason requirement, item immutability after payment, and extensible sorting requirements from Part 2 were all designed into the Part 1 architecture. This minimized code changes when Part 2 requirements were introduced. The state machine was built from the start, the cancellation reason field was included in the domain model, item immutability checks were placed in the domain layer, and sorting was abstracted behind a strategy interface.
- **Scope Deliberately Omitted.** The following are out of scope: authentication and authorization, API documentation generation (Swagger/OpenAPI), containerization (Docker), integration tests with testcontainers, audit logging, event sourcing, and CQRS.
- **Future Improvements.** Given more time, the following improvements would be made:
  - Add Spring Security with role based access control
  - Add OpenAPI/Swagger documentation
  - Add containerization with Docker and Docker Compose
  - Add integration tests with testcontainers for repository testing
  - Add audit logging for order status changes
  - Add idempotency keys for create requests to prevent duplicate orders
  - Implement rate limiting for API endpoints
  - Add metrics and health checks with Spring Boot Actuator

---

## Submission

This project is submitted as part of the technical test for the Backend Engineer Intern position at PT Astra Digital Mobil through the Astra Internship Fair (AIF) 2026.

| Identity  | Detail                       |
|-----------|------------------------------|
| Applicant | Raihan Putra Kirana          |
| Position  | Backend Engineer Intern      |
| Company   | PT Astra Digital Mobil (OLX) |

---