package com.kris007iron.modulithimpl.catalog.internal.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID productId) {
        super("Product with ID '%s' doesn't exist".formatted(productId));
    }
}
