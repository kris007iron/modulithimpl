package com.kris007iron.modulithimpl.inventory.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByProductIdAndLocationId(UUID productId, UUID locationId);
}
