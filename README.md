# Order Management
Utilizing modulith to showcase clear boundaries for modules and what they are responsible for.
## Modules

### Catalog

**Status: V1 Ready**

Owns product definitions and product-related operations.

#### Implemented
- Product creation, lookup, and deletion.
- SKU uniqueness enforced by a database constraint.
- REST API and exception handling.

#### Worth considering
- Input validation using `@Valid` and Bean Validation annotations.
- Additional database constraints and tests for concurrent requests.

### Inventory

**Status: V1 Ready**

Owns stock quantities, locations, and inventory reservations.

#### Implemented
- Stock creation, quantity updates, and lookup by product and location IDs.
- Stock reservation and reservation tracking by order ID.
- Pessimistic locking during stock reservation.
- Insufficient-stock handling.
- Transactional stock reservation.
- Protection against overselling when competing orders reserve stock concurrently.
- Idempotent reservation processing, including concurrent duplicate processing of the same order.
- Atomic reservation across multiple products: if any required product has insufficient stock, no stock is reserved.

#### Known limitations
- **Stock locations:** An order must be fulfillable from a single location.
- **Reservation processing:** failure recovery needs evaluation

### Orders

**Status: V1 Ready**

Owns order creation, order items, and order state transitions.

#### Implemented
- Order creation and validation when placing an order.
- Asynchronous processing using application events.
- Order confirmation after successful stock reservation.
- Order rejection when stock reservation fails.
- Handling of invalid and repeated state transitions.

#### Order lifecycle

`CREATED → PLACED → CONFIRMED / REJECTED`

Placing an order does not guarantee immediate confirmation. Its final state depends on the inventory reservation outcome.

### Notifications

**Status: V0.1 Ready**

Logs order processing outcomes through asynchronous application event listeners.

#### Implemented
- Logging successful stock reservation outcomes.
- Logging failed stock reservation outcomes.

#### Potential improvements
- Delivery mechanism and failure handling.
- External notification delivery e.g. via email.

## Order workflow

The application uses asynchronous Spring application events to coordinate order placement and inventory reservation.

1. An order is created and populated with items.
2. Orders validates the order, transitions it to `PLACED`, and publishes `OrderPlaced`.
3. Inventory attempts to reserve the required stock within a transaction.
4. On success, `StockReserved` is published and Orders transitions the order to `CONFIRMED`.
5. If stock cannot be reserved, `StockReservationFailed` is published and Orders transitions the order to `REJECTED`.

Inventory owns stock and reservation state, while Orders owns order state. This separation allows each module to maintain its own responsibilities without directly manipulating another module's internal entities.

The event contracts currently reside in the Orders module to keep the module dependency graph acyclic. Inventory consumes the placement event and publishes reservation outcomes through the exposed event contracts without depending on Orders' internal implementation.

## Concurrency and Consistency

Inventory reservation uses database transactions and pessimistic locking to protect stock updates when multiple orders compete for the same inventory.

Reservation processing also uses an atomic database operation to prevent concurrent duplicate processing of the same order from creating duplicate reservations.

These mechanisms are covered by PostgreSQL-backed tests for competing orders, duplicate event processing, and insufficient stock across multiple products.

## Technology Stack

- Java
- Spring Boot
- Spring Modulith
- Spring Data JPA and Hibernate
- PostgreSQL
- Flyway
- Maven
- Docker and Docker Compose
- JUnit and Spring testing support

## Database

- PostgreSQL is used for persistence.
- Flyway manages application database schema migrations.
- Hibernate uses `ddl-auto: validate` to validate entity mappings against the database schema.
- Spring Modulith initializes its JDBC event publication registry.

The initial application schema is defined in `V1__initial_schema.sql`.

## Getting Started

### Prerequisites

- Docker with Docker Compose.
- Git, if cloning the repository.

### Configuration

Create a `.env` file in the project root with the database credentials expected by `compose.yaml`. For example:

```dotenv
POSTGRES_DB=modulithimpl
POSTGRES_USER=myuser
POSTGRES_PASSWORD=replace_with_a_local_password
```

Do not commit `.env` or real credentials to version control.

Ensure that the environment variable names match those referenced by `compose.yaml`.

### Run the application

Build and start the application and PostgreSQL database:

```bash
docker compose up --build -d
```

The API is available at:

`http://localhost:8080`

View application logs:

```bash
docker compose logs -f app
```

Stop the containers:

```bash
docker compose down
```

Docker Compose uses a named PostgreSQL volume to persist database data across container restarts. Removing the volume with `docker compose down -v` permanently deletes the data stored in that volume.

## Testing

Run the full test suite using the Maven Wrapper:

**Windows:**
```powershell
.\mvnw.cmd test
```

**Linux/macOS:**
```bash
./mvnw test
```

The test suite includes:
- Service and event-processing tests.
- REST controller tests.
- PostgreSQL-backed persistence tests.
- Integration tests for successful reservation and insufficient-stock rejection.
- Concurrency tests for competing orders and duplicate processing.
- Spring Modulith architecture verification.

## Future improvements

- Define and verify recovery behavior for unexpected failures and event retries.
- Extend input validation and database constraints where appropriate.
- Evaluate Hibernate Envers for entity auditing.
