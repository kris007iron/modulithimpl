package com.kris007iron.modulithimpl.inventory.internal;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
    @Modifying
    @Query(value = """
    INSERT INTO inventory_reservation
        (order_id, location_id, reservation_status)
    VALUES (:orderId, NULL, :status)
    ON CONFLICT (order_id) DO NOTHING
    """, nativeQuery = true)
    int tryCreateReservation(
            @Param("orderId") UUID orderId,
            @Param("status") String status
    );
}
