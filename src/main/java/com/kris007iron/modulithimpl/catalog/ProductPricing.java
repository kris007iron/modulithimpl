package com.kris007iron.modulithimpl.catalog;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record ProductPricing(UUID productId, BigDecimal price, Currency currency) {
}
