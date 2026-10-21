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

**Status: V0.9 Ready**

Owns stock quantities, locations, and inventory reservations.

#### Implemented
- Stock creation and quantity updates.
- Stock lookup by product and location IDs.
- Reservation of stock for placed orders and reservation tracking by order ID.
- Pessimistic locking during stock reservation.
- Insufficient-stock handling.
- Atomic stock reservation using database transactions.

#### Known limitations
- **Idempotency:** Reservation processing checks for an existing reservation and uses the order ID as its primary key. This handles ordinary duplicate deliveries, but concurrent processing of the same order is not yet fully serialized.
- **Stock locations:** An order must be fulfillable from a single location.

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

**Status: Planned**

Intended to notify interested parties about order states and processing outcomes.

#### Implementation plan
- Notification handling.
- Delivery mechanism and failure handling.
- Tests for notification processing.

## Order workflow

1. An order is created and populated with items.
2. Orders validates the order and publishes `OrderPlaced`.
3. Inventory attempts to reserve the required stock.
4. On success, `StockReserved` is published and Orders transitions the order to `CONFIRMED`.
5. If stock cannot be reserved, `StockReservationFailed` is published and Orders transitions the order to `REJECTED`.

The workflow uses asynchronous application event listeners. Inventory owns stock and reservation state, while Orders owns order state.

The event contracts currently reside in the Orders module to keep the module dependency graph acyclic. Inventory consumes the placement event and publishes reservation outcomes using these public contracts, without depending on Orders' internal implementation.

## Testing

- Unit tests for services and event listeners.
- REST controller tests.
- PostgreSQL-backed persistence tests.
- End-to-end integration tests for successful reservation and insufficient-stock rejection.
- Spring Modulith architecture verification.

## Future improvements

- Strengthen idempotency under concurrent processing and test concurrent reservations.
- Define recovery behavior for unexpected failures and event retries.
- Introduce Flyway database migrations.
- Implement the Notifications module.
