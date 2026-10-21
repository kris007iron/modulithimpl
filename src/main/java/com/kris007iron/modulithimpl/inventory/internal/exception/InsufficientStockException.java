package com.kris007iron.modulithimpl.inventory.internal.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, UUID locationId) {
        super("Insufficient stock for product '%s' at location '%s'".formatted(productId, locationId));
    }

    public InsufficientStockException(UUID orderId) {
        super("No single location can fulfill order '%s'".formatted(orderId));
    }
}
