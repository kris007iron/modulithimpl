package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.orders.StockReservationFailed;
import com.kris007iron.modulithimpl.orders.StockReserved;
import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPlacedListener {
    private final InventoryService inventoryService;
    private final RejectionService rejectionService;
    private final ApplicationEventPublisher eventPublisher;

    @ApplicationModuleListener
    void on(OrderPlaced event) {
        try {
            inventoryService.reserveOrder(event);
            eventPublisher.publishEvent(new StockReserved(event.orderId()));
        } catch (InsufficientStockException exception) {
            rejectionService.recordRejection(event.orderId());
            eventPublisher.publishEvent(new StockReservationFailed(event.orderId(), exception.getMessage()));
        }
    }
}
