package com.kris007iron.modulithimpl.orders.internal;

import com.kris007iron.modulithimpl.catalog.CatalogApi;
import com.kris007iron.modulithimpl.catalog.ProductPricing;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import com.kris007iron.modulithimpl.orders.internal.exception.OrderNotFoundException;
import com.kris007iron.modulithimpl.orders.internal.exception.OrderProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class OrderServiceTest {
    private OrderService orderService;

    @MockitoBean
    private CatalogApi catalogApi;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        orderService = new OrderService(orderRepository, catalogApi, applicationEventPublisher);
    }

    @Test
    void shouldCreateOrderWithPriceFromCatalog() {
        UUID productId = UUID.randomUUID();

        when(catalogApi.findProductPricing(productId))
                .thenReturn(Optional.of(new ProductPricing(
                        productId,
                        new BigDecimal("19.99"),
                        Currency.getInstance("EUR")
                )));

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(productId, 2L))
        );

        Order order = orderService.createOrder(request);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(1);

        OrderItem item = order.getItems().getFirst();

        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(2L);
        assertThat(item.getUnitPrice())
                .isEqualByComparingTo("19.99");
        assertThat(item.getCurrency())
                .isEqualTo(Currency.getInstance("EUR"));

        Order persistedOrder = orderRepository
                .findById(order.getId())
                .orElseThrow();

        assertThat(persistedOrder.getItems().getFirst().getUnitPrice())
                .isEqualByComparingTo("19.99");
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        CreateOrderRequest request = new CreateOrderRequest(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An order must contain at least one item");
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExistInCatalog() {
        UUID productId = UUID.randomUUID();

        when(catalogApi.findProductPricing(productId))
                .thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(productId, 1L))
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OrderProductNotFoundException.class);

        assertThat(orderRepository.count()).isZero();
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

    @Test
    void shouldPlaceCreatedOrderAndPublishEvent() {
        Order savedOrder = createAndSaveNonEmptyOrder();

        orderService.placeOrder(savedOrder.getId());

        Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PLACED);

        ArgumentCaptor<OrderPlaced> eventCaptor =
                ArgumentCaptor.forClass(OrderPlaced.class);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        OrderPlaced event = eventCaptor.getValue();

        assertThat(event.orderId()).isEqualTo(savedOrder.getId());
        assertThat(event.items()).containsExactly(
                new OrderPlaced.OrderLine(
                        savedOrder.getItems().getFirst().getProductId(),
                        2L
                )
        );
    }

    @Test
    void shouldThrowWhenPlacingNonexistentOrder() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.placeOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldNotPlaceOrderTwice() {
        Order savedOrder = createAndSaveNonEmptyOrder();

        orderService.placeOrder(savedOrder.getId());

        // Clear the event published by the first placement.
        clearInvocations(applicationEventPublisher);

        assertThatThrownBy(() -> orderService.placeOrder(savedOrder.getId()))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldRejectPlacingOrderWithoutItems() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        assertThatThrownBy(() -> orderService.placeOrder(savedOrder.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An order must contain at least one item");

        Order unchangedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldNotPlaceCancelledOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        assertThatThrownBy(() -> orderService.placeOrder(savedOrder.getId()))
                .isInstanceOf(IllegalStateException.class);

        Order unchangedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldConfirmPlacedOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        orderService.confirmOrder(savedOrder.getId());

        Order updatedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldNotConfirmCancelledOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        orderService.confirmOrder(savedOrder.getId());

        Order unchangedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldRejectPlacedOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        orderService.rejectOrder(savedOrder.getId());

        Order updatedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void shouldNotRejectConfirmedOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.saveAndFlush(order);

        orderService.rejectOrder(savedOrder.getId());

        Order unchangedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();

        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldThrowWhenConfirmingNonexistentOrder() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.confirmOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenRejectingNonexistentOrder() {
        UUID orderId = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.rejectOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private Order createAndSaveNonEmptyOrder() {
        UUID productId = UUID.randomUUID();

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(productId);
        orderItem.setQuantity(2L);
        orderItem.setUnitPrice(new BigDecimal("19.99"));
        orderItem.setCurrency(Currency.getInstance("EUR"));

        order.setItems(List.of(orderItem));

        return orderRepository.saveAndFlush(order);
    }

    private Order createAndSaveOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        return orderRepository.save(order);
    }
}