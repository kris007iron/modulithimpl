package com.kris007iron.modulithimpl.notifications.internal;

import com.kris007iron.modulithimpl.orders.StockReservationFailed;
import com.kris007iron.modulithimpl.orders.StockReserved;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderNotificationListener {
    @ApplicationModuleListener
    void on(StockReserved stockReserved) {
        log.info("Order {} confirmed successfully", stockReserved.orderId());
    }

    @ApplicationModuleListener
    void on(StockReservationFailed stockReservationFailed) {
        log.warn("Order {} was rejected due to insufficient stock", stockReservationFailed.orderId());
    }
}
