package com.kris007iron.modulithimpl.inventory.internal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(RejectionService.class)
public class RejectionServiceTest {

    @Autowired
    private RejectionService reservationService;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldPersistRejection() {
        UUID orderId = UUID.randomUUID();

        reservationService.recordRejection(orderId);

        InventoryReservation reservation =
                reservationRepository.findById(orderId).orElseThrow();

        assertEquals(
                ReservationStatus.REJECTED,
                reservation.getReservationStatus()
        );
        assertNull(reservation.getLocationId());
    }

    @Test
    void shouldPersistRejectionEvenWhenOuterTransactionRollsBack() {
        UUID orderId = UUID.randomUUID();

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            reservationService.recordRejection(orderId);
            status.setRollbackOnly();
        });

        InventoryReservation reservation =
                reservationRepository.findById(orderId).orElseThrow();

        assertEquals(
                ReservationStatus.REJECTED,
                reservation.getReservationStatus()
        );
        assertNull(reservation.getLocationId());
    }
}
