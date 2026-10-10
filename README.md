# Order Management
Utilizing modulith to showcase clear boundaries for modules and what they are responsible for.
## Modules:
### Catalog
Product definition V1 Ready
Worth to consider to implement:
Validation utilizing @Valid and other annotations
Concurrency/DB constraints apart from simple unique marker for sku also something for concurrent requests
### Inventory
Holds magazine information V1 Ready
Reservation behavior will be implemented alongside orders and its events
### Orders
Order creation V0.5 Ready
An event requesting catalog item from inventory
### Notifications
Inform those who listen about state of orders and how they are processed
