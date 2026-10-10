package com.kris007iron.modulithimpl.orders.internal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateOrderItemRequest(@NotNull(message = "Product ID is required") UUID productId, @Positive(message = "Quantity must be greater than zero") Long quantity) {
}
