# Order Service Implementation Plan

## Tech Stack

| Component        | Version | Rationale                                                             |
|------------------|---------|-----------------------------------------------------------------------|
| Java             | 21 LTS  | Latest LTS JDK with long term support                                 |
| Spring Boot      | 4.0.7   | Latest 4.0.x LTS patch                                                |
| Spring Framework | 7.0.x   | Bundled with Spring Boot 4.0.7                                        |
| Gradle           | 8.x     | Kotlin DSL, consistent with project conventions                       |
| H2 Database      | 2.x     | In memory for development, file based for persistence across restarts |
| JUnit 5          | 5.x     | Standard for Spring Boot testing                                      |
| Mockito          | 5.x     | Mocking framework for unit tests                                      |

The latest Spring Boot LTS branch is `4.0.x` which has been available since November 2025 (7 months of production usage). Version `4.0.7` is the latest patch release as of June 2026. Spring Boot `3.5.x` OSS support ended June 30 2026.

---

## Architecture: Vertical Slice with Domain Driven Design

### Why Vertical Slice Architecture

Traditional layered architecture (controller, service, repository) spreads a single feature across multiple horizontal layers. When features grow, every change touches every layer. Vertical slice architecture organizes code by domain feature instead. Each slice is a complete vertical containing its own domain model, application logic, and infrastructure. This makes the codebase navigable, testable, and extensible.

For a single domain service like Order Service, there is one vertical slice (order). The structure scales naturally when new domains are added later.

### Project Structure

```
order-service/
+ src/
  + main/
  ! + java/com/aif/orderservice/
  !   + OrderServiceApplication.java
  !   + config/
  !   ! + JacksonConfig.java
  !   ! + OpenApiConfig.java
  !   + order/
  !     + domain/
  !     ! + model/
  !     ! ! + Order.java           (aggregate root)
  !     ! ! + OrderStatus.java     (enum with lifecycle)
  !     ! ! + LineItem.java        (value object)
  !     ! + service/
  !     ! ! + OrderDomainService.java  (pure domain logic, no framework)
  !     ! + port/
  !     !   + OrderRepository.java     (interface only)
  !     + application/
  !     ! + dto/
  !     ! ! + CreateOrderRequest.java
  !     ! ! + UpdateOrderRequest.java
  !     ! ! + CancelOrderRequest.java
  !     ! ! + OrderResponse.java
  !     ! ! + PagedOrderResponse.java
  !     ! ! + ErrorResponse.java
  !     ! + service/
  !     ! ! + OrderApplicationService.java  (use cases, transaction boundary)
  !     ! ! + OrderMapper.java              (domain to DTO mapping)
  !     ! + exception/
  !     !   + OrderNotFoundException.java
  !     !   + InvalidOrderException.java
  !     !   + IllegalStatusTransitionException.java
  !     !   + OrderItemModificationException.java
  !     + infrastructure/
  !       + persistence/
  !       ! + JpaOrderRepository.java    (Spring Data JPA impl)
  !       ! + OrderEntity.java           (JPA entity)
  !       ! + OrderLineItemEntity.java   (JPA entity)
  !       ! + OrderPersistenceMapper.java (entity to domain mapping)
  !       + web/
  !         + OrderController.java       (REST endpoints)
  !         + GlobalExceptionHandler.java (@ControllerAdvice)
  ! + resources/
  !   + application.yml
  + test/
    + java/com/aif/orderservice/
      + order/
        + domain/
        ! + OrderTest.java
        ! + OrderDomainServiceTest.java
        ! + LineItemTest.java
        + application/
        ! + OrderApplicationServiceTest.java
        + infrastructure/
          + web/
            + OrderControllerTest.java
            + GlobalExceptionHandlerTest.java
```

### Layer Responsibilities

**Domain Layer** (pure Java, zero framework imports)
- `Order` aggregate root with behavior (not an anemic model)
- `OrderStatus` enum with transition rules
- `LineItem` value object with validation
- `OrderDomainService` for complex domain operations
- `OrderRepository` interface (port)

**Application Layer**
- DTOs for request and response payloads
- `OrderApplicationService` orchestrating use cases
- `OrderMapper` converting between domain and DTO
- Domain exceptions

**Infrastructure Layer**
- JPA entities and repository implementation
- REST controller
- Global exception handler for consistent error responses

---

## Domain Model

### Order Aggregate Root

```
Order
  orderId: UUID           (server generated, never accepted from client)
  customerName: String    (required, non blank)
  lineItems: List<LineItem>  (at least one item)
  status: OrderStatus     (defaults to CREATED)
  totalAmount: BigDecimal (server computed from line items)
  cancellationReason: String (nullable, required when status is CANCELLED)
  createdAt: Instant      (server managed)
  updatedAt: Instant      (server managed)
```

### OrderStatus Enum

```
CREATED -> PAID -> SHIPPED -> DELIVERED
CREATED -> CANCELLED
PAID -> CANCELLED
SHIPPED -> CANCELLED
DELIVERED  (terminal, no transitions out)
CANCELLED  (terminal, no transitions out)
```

Transitions not listed above are illegal and must be rejected.

### LineItem Value Object

```
LineItem
  productName: String    (required, non blank)
  quantity: int          (positive, at least 1)
  unitPrice: BigDecimal  (positive, scale 2)
```

### Total Amount Computation

The server computes `totalAmount` as the sum of `quantity * unitPrice` across all line items. The client cannot supply `totalAmount`. If provided, it is ignored or rejected (see security considerations).

---

## API Design

| Method | Endpoint                     | Description                             | Status Codes                                         |
|--------|------------------------------|-----------------------------------------|------------------------------------------------------|
| POST   | /api/orders                  | Create a new order                      | 201 Created, 400 Bad Request                         |
| GET    | /api/orders/{orderId}        | Get order by ID                         | 200 OK, 404 Not Found                                |
| GET    | /api/orders                  | List orders with pagination and sorting | 200 OK                                               |
| PUT    | /api/orders/{orderId}        | Update order (customerName, items)      | 200 OK, 400 Bad Request, 404 Not Found               |
| PATCH  | /api/orders/{orderId}/status | Transition order status (cancel)        | 200 OK, 400 Bad Request, 404 Not Found, 409 Conflict |
| DELETE | /api/orders/{orderId}        | Delete an order                         | 204 No Content, 404 Not Found                        |

### Sorting Strategies (Part 2 Design)

Sort parameter uses strategy pattern:

| Sort Value    | Behavior                                            |
|---------------|-----------------------------------------------------|
| newest        | Sort by createdAt descending                        |
| oldest_unpaid | Sort by createdAt ascending where status is CREATED |
| highest_total | Sort by totalAmount descending                      |

Adding a new sort strategy requires only adding a new enum constant and its `Sort` implementation. No existing code is modified.

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

---

## Part 1 Implementation Steps

### Step 1: Initialize Spring Boot Project

Create Gradle project with `Spring Boot 4.0.7`, `Java 21`. Dependencies:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `h2` (runtime)
- `spring-boot-starter-test`
- `spring-boot-starter-validation`

### Step 2: Domain Model

Create the pure domain classes:
1. `OrderStatus` enum with transition validation method
2. `LineItem` record with constructor validation
3. `Order` class with behavior methods (`addItem`, `removeItem`, `cancel`, `pay`, `ship`, `deliver`)

### Step 3: Repository Port Interface

Define `OrderRepository` interface in `domain/port` with CRUD methods.

### Step 4: Application Service and DTOs

1. Create request DTOs with `Jakarta Validation` annotations
2. Create response DTOs
3. Create `OrderApplicationService` implementing use cases
4. Create `OrderMapper` for domain to DTO conversion

### Step 5: Infrastructure Persistence

1. Create JPA entity classes (separate from domain model)
2. Implement `JpaOrderRepository` extending `JpaRepository`
3. Create `OrderPersistenceMapper` for bidirectional mapping

### Step 6: REST Controller

1. Create `OrderController` with all endpoints
2. Create `GlobalExceptionHandler` with `@ControllerAdvice`

### Step 7: Unit Tests

1. `OrderTest`: domain behavior, status transitions
2. `LineItemTest`: value object validation
3. `OrderDomainServiceTest`: complex domain rules
4. `OrderApplicationServiceTest`: use cases with mocked repository
5. `OrderControllerTest`: REST endpoint behavior

---

## Part 2 Extensions (Designed from Start)

The following Part 2 requirements are accommodated in the Part 1 design:

| Part 2 Requirement                   | Part 1 Design Decision                                                                                                                       |
|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| Constrained status transitions       | `OrderStatus` enum has built in transition validation. `Order` class enforces transitions in behavior methods.                                   |
| Cancellation requires reason         | `CancelOrderRequest` includes `cancellationReason` field. `cancel` method requires reason parameter.                                               |
| Items immutable after payment        | `Order` has `isItemsModifiable()` check based on status. `OrderApplicationService` rejects item changes when status is `PAID` or beyond.                            |
| New ordering rules without rewriting | Strategy pattern: sorting is abstracted behind an interface. Each rule is a separate implementation. Adding a rule creates a new class only. |

---

## Assumptions and Design Decisions

1. **Total Amount is server computed.** The client never supplies `totalAmount`. The server calculates it as the sum of `quantity * unitPrice` per line item using `BigDecimal` with scale 2 and `HALF_UP` rounding.

2. **Currency is not modeled.** All monetary values are in a single assumed currency (IDR or USD). Currency conversion is out of scope.

3. **H2 database in file mode.** Data persists across restarts using file based H2. No external database setup is needed.

4. **UUID validation.** The server generates `orderId` as `UUID`. Any `orderId` supplied by the client in create request is ignored. In update and delete, the path variable `orderId` is used.

5. **Delete is hard delete.** `DELETE` permanently removes the order from storage. Soft delete is not required.

6. **Cancel versus Delete.** Cancel transitions status to `CANCELLED`. Delete removes the record entirely. They are separate operations.

7. **Pagination defaults.** Default page is 0, default size is 20, default sort is `newest`.

8. **Status transition on update.** Updating order fields (`customerName`, `items`) does not change status. Status change is a separate `PATCH` operation.

9. **Unknown sort parameter defaults to newest.** If the sort parameter value is not recognized, the server defaults to `newest` sorting.

---

## Testing Strategy

| Test Class                    | What It Tests                                                | Type      |
|-------------------------------|--------------------------------------------------------------|-----------|
| `OrderTest`                   | Domain state transitions, total computation, item management | Unit      |
| `LineItemTest`                | Value object validation, negative quantity rejection         | Unit      |
| `OrderDomainServiceTest`      | Business rules, illegal transitions                          | Unit      |
| `OrderApplicationServiceTest` | Use case orchestration with mocked repository                | Unit      |
| `OrderControllerTest`         | HTTP mapping, status codes, validation errors                | Web slice |

All tests run with `./gradlew test`.

---

## Deliverables

1. Complete source code with Gradle build files
2. README.md with versions, build/run instructions, API documentation, design decisions
3. Passing test suite
4. PLAN.md (this document)
