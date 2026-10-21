
package com.kris007iron.modulithimpl.orders.internal;

import com.kris007iron.modulithimpl.orders.StockReservationFailed;
import com.kris007iron.modulithimpl.orders.StockReserved;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StockReservationListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private StockReservationListener listener;

    @Test
    void shouldConfirmOrderWhenStockIsReserved() {
        UUID orderId = UUID.randomUUID();
        StockReserved event = new StockReserved(orderId);

        listener.on(event);

        verify(orderService).confirmOrder(orderId);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void shouldRejectOrderWhenStockReservationFails() {
        UUID orderId = UUID.randomUUID();
        StockReservationFailed event =
                new StockReservationFailed(orderId, "Insufficient stock");

        listener.on(event);

        verify(orderService).rejectOrder(orderId);
        verifyNoMoreInteractions(orderService);
    }
}
