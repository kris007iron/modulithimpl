package com.kris007iron.modulithimpl.orders.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        List<OrderItem> items = request.items().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemRequest.productId());
                    item.setQuantity(itemRequest.quantity());
                    return item;
                })
                .toList();

        order.setItems(items);

        return orderRepository.save(order);
    }

    public Optional<Order> findOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }
}
