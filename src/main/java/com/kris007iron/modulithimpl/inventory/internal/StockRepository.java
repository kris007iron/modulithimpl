package com.kris007iron.modulithimpl.inventory.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByProductIdAndLocationId(UUID productId, UUID locationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Stock> findByProductIdInOrderByLocationIdAsc(Collection<UUID> productIds);
}
