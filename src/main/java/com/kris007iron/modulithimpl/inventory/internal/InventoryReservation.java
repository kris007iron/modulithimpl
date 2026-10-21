package com.kris007iron.modulithimpl.inventory.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class InventoryReservation {
    @Id
    private UUID orderId;
    private UUID locationId;
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
}
