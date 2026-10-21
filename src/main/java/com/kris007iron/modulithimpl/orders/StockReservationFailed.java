package com.kris007iron.modulithimpl.orders;

import java.util.UUID;

public record StockReservationFailed(UUID orderId, String reason) {
}
