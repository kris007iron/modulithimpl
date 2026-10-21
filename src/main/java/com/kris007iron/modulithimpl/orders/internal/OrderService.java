package com.kris007iron.modulithimpl.orders.internal;

import com.kris007iron.modulithimpl.catalog.CatalogApi;
import com.kris007iron.modulithimpl.catalog.ProductPricing;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import com.kris007iron.modulithimpl.orders.internal.exception.OrderNotFoundException;
import com.kris007iron.modulithimpl.orders.internal.exception.OrderProductNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogApi catalogApi;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Order createOrder(@NonNull CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "An order must contain at least one item");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        List<OrderItem> items = request.items().stream()
                .map(itemRequest -> {
                    ProductPricing pricing = catalogApi
                            .findProductPricing(itemRequest.productId())
                            .orElseThrow(() ->
                                    new OrderProductNotFoundException(
                                            itemRequest.productId()));

                    OrderItem item = new OrderItem();
                    item.setProductId(pricing.productId());
                    item.setQuantity(itemRequest.quantity());
                    item.setUnitPrice(pricing.price());
                    item.setCurrency(pricing.currency());

                    return item;
                })
                .toList();

        order.setItems(new ArrayList<>(items));

        return orderRepository.save(order);
    }

    @Transactional
    public Order placeOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED orders can be placed"
            );
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }

        order.setStatus(OrderStatus.PLACED);

        applicationEventPublisher.publishEvent(new OrderPlaced(
                order.getId(),
                order.getItems().stream()
                        .map(item -> new OrderPlaced.OrderLine(
                                item.getProductId(),
                                item.getQuantity()
                        ))
                        .toList()
        ));

        return order;
    }

    public Optional<Order> findOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public void confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PLACED) {
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
    }

    @Transactional
    public void rejectOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PLACED) {
            return;
        }

        order.setStatus(OrderStatus.REJECTED);
    }
}
