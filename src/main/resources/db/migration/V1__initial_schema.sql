CREATE TABLE inventory_reservation
(
    order_id           UUID NOT NULL,
    location_id        UUID,
    reservation_status VARCHAR(255),
    CONSTRAINT pk_inventoryreservation PRIMARY KEY (order_id)
);

CREATE TABLE order_items
(
    id         UUID   NOT NULL,
    order_id   UUID   NOT NULL,
    product_id UUID,
    quantity   BIGINT NOT NULL,
    unit_price DECIMAL,
    currency   VARCHAR(255),
    CONSTRAINT pk_order_items PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id         UUID NOT NULL,
    status     VARCHAR(255),
    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE TABLE products
(
    id          UUID         NOT NULL,
    sku         VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    description VARCHAR(2000),
    price       DECIMAL,
    currency    VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE stocks
(
    id                UUID NOT NULL,
    product_id        UUID,
    location_id       UUID,
    quantity          BIGINT,
    reserved_quantity BIGINT,
    CONSTRAINT pk_stocks PRIMARY KEY (id)
);

ALTER TABLE stocks
    ADD CONSTRAINT uc_8c66666b1702fef1baa6ab204 UNIQUE (product_id, location_id);

ALTER TABLE products
    ADD CONSTRAINT uc_products_sku UNIQUE (sku);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);