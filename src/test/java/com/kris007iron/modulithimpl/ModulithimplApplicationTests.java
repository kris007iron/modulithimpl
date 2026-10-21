package com.kris007iron.modulithimpl;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import com.kris007iron.modulithimpl.catalog.internal.CatalogService;
import com.kris007iron.modulithimpl.catalog.internal.ProductDTO;
import com.kris007iron.modulithimpl.inventory.internal.*;
import com.kris007iron.modulithimpl.orders.internal.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ModulithimplApplicationTests {
	@Autowired
	private CatalogService catalogService;
	@Autowired
	private InventoryService inventoryService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private InventoryReservationRepository inventoryReservationRepository;

	@Test
	void shouldConfirmOrderAfterSuccessfulStockReservation() {
		UUID locationId = UUID.randomUUID();
		ProductDTO productDTO = new ProductDTO(
				"IT-" + UUID.randomUUID(),
				"Integration test product",
				"Product for the integration test",
				new BigDecimal("19.98"),
				Currency.getInstance("PLN")
		);
		ProductReference product = catalogService.addProduct(productDTO);

		inventoryService.addStock(product.productId(), locationId, 10L);

		Order order = orderService.createOrder(new CreateOrderRequest(List.of(
				new CreateOrderItemRequest(product.productId(), 2L)
		)));

		orderService.placeOrder(order.getId());

		await()
				.atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> {
					Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
					assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

					Stock stock = stockRepository.findByProductIdAndLocationId(product.productId(), locationId).orElseThrow();

					assertThat(stock.getQuantity()).isEqualTo(10L);
					assertThat(stock.getReservedQuantity()).isEqualTo(2L);

					InventoryReservation reservation = inventoryReservationRepository.findById(order.getId()).orElseThrow();

					assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.RESERVED);
					assertThat(reservation.getLocationId()).isEqualTo(locationId);
				});
	}

	@Test
	void shouldRejectOrderWhenStockIsInsufficient() {
		UUID locationId = UUID.randomUUID();

		ProductDTO productDTO = new ProductDTO(
				"IT-" + UUID.randomUUID(),
				"Integration test product",
				"Product used for integration testing",
				new BigDecimal("19.99"),
				Currency.getInstance("EUR")
		);

		ProductReference product = catalogService.addProduct(productDTO);

		inventoryService.addStock(product.productId(), locationId, 1L);

		Order order = orderService.createOrder(
				new CreateOrderRequest(List.of(
						new CreateOrderItemRequest(product.productId(), 2L)
				))
		);

		orderService.placeOrder(order.getId());

		await()
				.atMost(Duration.ofSeconds(20))
				.untilAsserted(() -> {
					Order updatedOrder = orderRepository
							.findById(order.getId())
							.orElseThrow();

					assertThat(updatedOrder.getStatus())
							.isEqualTo(OrderStatus.REJECTED);

					Stock stock = stockRepository
							.findByProductIdAndLocationId(
									product.productId(), locationId)
							.orElseThrow();

					assertThat(stock.getQuantity()).isEqualTo(1L);
					assertThat(stock.getReservedQuantity()).isZero();
				});
	}
}
