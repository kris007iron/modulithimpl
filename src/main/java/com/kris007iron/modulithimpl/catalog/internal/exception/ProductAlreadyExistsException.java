package com.kris007iron.modulithimpl.catalog.internal.exception;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String sku) {
        super("Product with SKU '%s' already exists".formatted(sku));
    }
}
