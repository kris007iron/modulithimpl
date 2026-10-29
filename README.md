# Order Management
Utilizing modulith to showcase clear boundaries for modules and what they are responsible for.
## Modules

### Catalog

**Status: V1 Ready**

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
- Stock creation and quantity updates.
- Stock lookup by product and location IDs.
- Reservation of stock for placed orders and reservation tracking by order ID.
- Pessimistic locking during stock reservation.
- Insufficient-stock handling.
- Atomic stock reservation using database transactions.
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

#### Key behavior
Placing an order does not guarantee immediate confirmation. Its final state depends on the inventory reservation outcome.

### Notifications

**Status: V0.1 Ready**

Logs order processing outcomes through asynchronous application event listeners.

#### Implemented
- Logging successful stock reservation outcomes
- Logging failed stock reservation outcomes

#### Future improvements
- Delivery mechanism and failure handling.
- External notification delivery e.g. via email

## Order workflow

1. An order is created and populated with items.
2. Orders validates the order and publishes `OrderPlaced`.
3. Inventory attempts to reserve the required stock.
4. On success, `StockReserved` is published and Orders transitions the order to `CONFIRMED`.
5. If stock cannot be reserved, `StockReservationFailed` is published and Orders transitions the order to `REJECTED`.

The workflow uses asynchronous application event listeners. Inventory owns stock and reservation state, while Orders owns order state.

The event contracts currently reside in the Orders module to keep the module dependency graph acyclic. Inventory consumes the placement event and publishes reservation outcomes using these public contracts, without depending on Orders' internal implementation.

## Database
- PostgreSQL is used for persistence.
- Flyway manages application database schema migrations.
- Hibernate uses ddl-auto: validate to validate entity mappings against the database schema.
- Spring Modulith manages initialization of its JDBC event publication registry.

The initial schema is defined in V1__initial_schema.sql.

## Testing

- Service and event processing test.
- REST controller tests.
- PostgreSQL-backed persistence tests.
- End-to-end integration tests for successful reservation and insufficient-stock rejection.
- Spring Modulith architecture verification.

## Future improvements

- Define and verify recovery behavior for unexpected failures and event retries.
- Add deployment configuration and operational documentation.
- Evaluate Hibernate Envers for entity auditing.
- Extend input validation and database constraints where appropriate.