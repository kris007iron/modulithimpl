package com.kris007iron.modulithimpl.inventory.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RejectionService {
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRejection(UUID orderId) {
        if (inventoryReservationRepository.existsById(orderId)) {
            return;
        }

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(orderId);
        reservation.setLocationId(null);
        reservation.setReservationStatus(ReservationStatus.REJECTED);

        inventoryReservationRepository.save(reservation);
    }
}
