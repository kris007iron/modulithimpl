package com.kris007iron.modulithimpl.catalog.internal;

import java.math.BigDecimal;
import java.util.Currency;

public record ProductDTO(String sku, String name, String description, BigDecimal price, Currency currency) {
}
