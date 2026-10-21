package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.orders.StockReservationFailed;
import com.kris007iron.modulithimpl.orders.StockReserved;
import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith(MockitoExtension.class)
public class OrderPlacedListenerTest {
    @Mock
    private InventoryService inventoryService;

    @Mock
    private RejectionService rejectionService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private OrderPlacedListener orderPlacedListener;

    @Test
    void shouldPublishStockReservedWhenReservationSucceeds() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderPlaced event = new OrderPlaced(orderId,
                List.of(new OrderPlaced.OrderLine(productId, 3L)));

        orderPlacedListener.on(event);

        verify(inventoryService).reserveOrder(event);
        verify(applicationEventPublisher).publishEvent(new StockReserved(orderId));
        verify(applicationEventPublisher, never())
                .publishEvent(isA(StockReservationFailed.class));
    }

    @Test
    void shouldPublishFailureWhenStockIsInsufficient() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderPlaced event = new OrderPlaced(
                orderId,
                List.of(new OrderPlaced.OrderLine(productId, 3L))
        );

        doThrow(new InsufficientStockException(orderId))
                .when(inventoryService).reserveOrder(event);

        orderPlacedListener.on(event);

        verify(inventoryService).reserveOrder(event);
        verify(rejectionService).recordRejection(orderId);

        verify(applicationEventPublisher)
                .publishEvent(isA(StockReservationFailed.class));

        verify(applicationEventPublisher, never())
                .publishEvent(isA(StockReserved.class));
    }
}
