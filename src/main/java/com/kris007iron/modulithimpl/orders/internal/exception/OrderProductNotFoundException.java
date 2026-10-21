package com.kris007iron.modulithimpl.orders.internal.exception;

import java.util.UUID;

public class OrderProductNotFoundException extends RuntimeException {
    public OrderProductNotFoundException(UUID productId) {
        super("Product not found: " + productId);
    }
}
