package com.kris007iron.modulithimpl.orders.internal;

import com.kris007iron.modulithimpl.orders.StockReservationFailed;
import com.kris007iron.modulithimpl.orders.StockReserved;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockReservationListener {
    private final OrderService orderService;

    @ApplicationModuleListener
    void on(StockReserved stockReserved) {
        orderService.confirmOrder(stockReserved.orderId());
    }

    @ApplicationModuleListener
    void on(StockReservationFailed stockReservationFailed) {
        orderService.rejectOrder(stockReservationFailed.orderId());
    }
}
