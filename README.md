# Order Management Service

A Spring Boot microservice that manages Products and Orders.

## How to Run

**Prerequisites:** Java 17+, Maven 3.8+

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:9950`.

## How to Run Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest="ProductServiceTest,OrderServiceTest"

# Integration tests only
mvn test -Dtest="ProductControllerIT,OrderControllerIT"

# Single test method
mvn test -Dtest=OrderServiceTest#create_rejectsOrderWhenAnyProductIdMissing
```

## Authentication

All `/api/**` endpoints require HTTP Basic Auth.

| Username | Password |
|----------|--------|
| `admin`  | `pass` |

**Note:** Credentials are hardcoded for demo purposes.

## API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

Click **Authorize** and enter `admin` / `password` to make authenticated requests directly from the browser.

H2 Console (dev only): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

## Schema Design

Three tables:

- **products** — stores product catalogue (id, name, description, price)
- **orders** — stores order header (id, status, total_price, created_at)
- **order_items** — join table linking orders to products (order_id FK, product_id FK, quantity)

`total_price` is denormalized onto the `orders` row at creation time so it remains correct even if product prices change later.

## Assumptions

- A single in-memory H2 database; data is reset on restart.
- Order status is always `CREATED`; no status transitions are implemented.
