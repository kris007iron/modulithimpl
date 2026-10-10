package com.kris007iron.modulithimpl.orders.internal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(@NotEmpty(message = "An order must contain at least one item") @Valid List<CreateOrderItemRequest> items) {
}
