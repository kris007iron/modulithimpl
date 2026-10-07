package com.kris007iron.modulithimpl.inventory.internal.exception;

import java.util.UUID;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(UUID productId, UUID locationId) {
        super("Stock not found for product '%s' at location '%s'".formatted(productId, locationId));
    }
}
