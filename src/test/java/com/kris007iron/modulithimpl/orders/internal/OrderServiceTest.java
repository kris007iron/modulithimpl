package com.kris007iron.modulithimpl.orders.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderService.class)
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrder() {
        CreateOrderItemRequest item = new CreateOrderItemRequest(UUID.randomUUID(), 2L);
        CreateOrderRequest request = new CreateOrderRequest(List.of(item));
        Order order = orderService.createOrder(request);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().getProductId()).isEqualTo(item.productId());
        assertThat(order.getItems().getFirst().getQuantity()).isEqualTo(2L);
        assertThat(orderRepository.findById(order.getId())).isPresent();
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        CreateOrderRequest request = new CreateOrderRequest(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An order must contain at least one item");
    }

    @Test
    void shouldRejectOrderWithNullItems() {
        CreateOrderRequest request = new CreateOrderRequest(null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An order must contain at least one item");
    }

    @Test
    void shouldFindExistingOrder() {
        Order order = createAndSaveOrder();
        Optional<Order> result = orderService.findOrder(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        Optional<Order> result = orderService.findOrder(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    private Order createAndSaveOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        return orderRepository.save(order);
    }
}