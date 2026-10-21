package com.kris007iron.modulithimpl.orders;

import java.util.List;
import java.util.UUID;

public record OrderPlaced(UUID orderId, List<OrderLine> items) {
    public record OrderLine(UUID productId, Long quantity) {}
}
