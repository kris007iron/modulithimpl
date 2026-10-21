package com.kris007iron.modulithimpl.catalog;

import java.util.Optional;
import java.util.UUID;

public interface CatalogApi {
    Optional<ProductPricing> findProductPricing(UUID productId);
}
