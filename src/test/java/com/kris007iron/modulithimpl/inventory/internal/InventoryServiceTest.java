package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.inventory.internal.exception.StockNotFoundException;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InventoryService.class)
class InventoryServiceTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private StockRepository stockRepository;

    private final UUID testProductId = UUID.randomUUID();
    private final UUID testLocationId = UUID.randomUUID();
    private final long quantity = 1L;
    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;

    @BeforeEach
    void setUp() { stockRepository.deleteAll(); inventoryReservationRepository.deleteAll(); }

    @Test
    void shouldAddStock() {
        Stock stock = inventoryService.addStock(testProductId, testLocationId, quantity);

        assertThat(stock).isNotNull();
        assertThat(stock.getProductId()).isEqualTo(testProductId);

        Optional<Stock> saved = stockRepository.findByProductIdAndLocationId(testProductId, testLocationId);

        assertThat(saved).isPresent();
        assertThat(saved.get().getProductId()).isEqualTo(testProductId);
    }

    @Test
    void shouldAddQuantity() {
        Stock stock = inventoryService.addStock(testProductId, testLocationId, quantity);

        assertThat(stock).isNotNull();
        assertThat(stock.getQuantity()).isEqualTo(1L);

        stock = inventoryService.addStock(testProductId, testLocationId, quantity);
        Optional<Stock> saved = stockRepository.findByProductIdAndLocationId(testProductId, testLocationId);

        assertThat(stock).isNotNull();
        assertThat(stock.getQuantity()).isEqualTo(2L);
        assertThat(saved).isPresent();
        assertThat(saved.get().getQuantity()).isEqualTo(2L);
    }

    @Test
    void shouldRejectNegativeQuantityWhileAdding() {
        assertThatThrownBy(() -> inventoryService.addStock(testProductId, testLocationId, -1L)).isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldRemoveStock() {
        Stock stock = inventoryService.addStock(testProductId, testLocationId, quantity);

        assertThat(stock).isNotNull();
        assertThat(stock.getQuantity()).isEqualTo(1L);

        stock = inventoryService.removeStock(testProductId, testLocationId, quantity);

        assertThat(stock).isNotNull();
        assertThat(stock.getQuantity()).isEqualTo(0L);

        Optional<Stock> saved = stockRepository.findByProductIdAndLocationId(testProductId, testLocationId);

        assertThat(saved).isPresent();
        assertThat(saved.get().getQuantity()).isEqualTo(0L);
    }

    @Test
    void shouldRejectNegativeQuantityWhileRemoving() {
        assertThatThrownBy(() -> inventoryService.removeStock(testProductId, testLocationId, -1L)).isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectNotExistingWhileRemoving() {
        assertThatThrownBy(() -> inventoryService.removeStock(testProductId, testLocationId, quantity)).isInstanceOf(StockNotFoundException.class).hasMessage("Stock not found for product '%s' at location '%s'".formatted(testProductId, testLocationId));
    }

    @Test
    void shouldRejectNotSufficientWhileRemoving() {
        Stock stock = inventoryService.addStock(testProductId, testLocationId, quantity);
        assertThatThrownBy(() -> inventoryService.removeStock(testProductId, testLocationId, 2L)).isInstanceOf(InsufficientStockException.class).hasMessage("Insufficient stock for product '%s' at location '%s'".formatted(testProductId, testLocationId));
    }

    @Test
    void shouldFindStock() {
        Stock stock = inventoryService.addStock(testProductId, testLocationId, quantity);
        Optional<Stock> optionalStock = inventoryService.findStock(testProductId, testLocationId);

        assertThat(optionalStock).isPresent();
        assertThat(optionalStock.get().getProductId()).isEqualTo(testProductId);
    }

    @Test
    void shouldReturnEmptyWhenStockDoesNotExist() {
        assertThat(inventoryService.findStock(testProductId, testLocationId)).isEmpty();
    }

    @Test
    void shouldKeepStockSeparateForDifferentLocations() {
        UUID anotherLocationId = UUID.randomUUID();

        inventoryService.addStock(testProductId, testLocationId, 10L);
        inventoryService.addStock(testProductId, anotherLocationId, 20L);

        assertThat(inventoryService.findStock(testProductId, testLocationId))
                .get()
                .extracting(Stock::getQuantity)
                .isEqualTo(10L);

        assertThat(inventoryService.findStock(testProductId, anotherLocationId))
                .get()
                .extracting(Stock::getQuantity)
                .isEqualTo(20L);
    }

    @Test
    void shouldReserveStockWhenAvailable() {
        UUID orderId = UUID.randomUUID();
        Stock stock = createStock(testProductId, testLocationId, 10L, 0L);
        OrderPlaced event = createOrderPlacedEvent(orderId, testProductId, 3L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        inventoryService.reserveOrder(event);
        Stock updated = stockRepository.findByProductIdAndLocationId(testProductId, testLocationId).orElseThrow();

        assertThat(10L).isEqualTo(updated.getQuantity());
        assertThat(3L).isEqualTo(updated.getReservedQuantity());

        InventoryReservation reservation = inventoryReservationRepository.findById(orderId).orElseThrow();

        assertThat(ReservationStatus.RESERVED).isEqualTo(reservation.getReservationStatus());
        assertThat(reservation.getLocationId()).isEqualTo(testLocationId);
    }

    @Test
    void shouldThrowWhenStockIsInsufficient() {
        UUID orderId = UUID.randomUUID();
        createStock(testProductId, testLocationId, 5L, 2L);

        OrderPlaced event =
                createOrderPlacedEvent(orderId, testProductId, 4L);

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveOrder(event)
        );

        Stock unchanged = stockRepository
                .findByProductIdAndLocationId(testProductId, testLocationId)
                .orElseThrow();

        assertEquals(5L, unchanged.getQuantity());
        assertEquals(2L, unchanged.getReservedQuantity());

        assertFalse(inventoryReservationRepository.existsById(orderId));
    }

    @Test
    void shouldNotReserveStockTwiceForSameOrder() {
        UUID orderId = UUID.randomUUID();

        createStock(testProductId, testLocationId, 10L, 0L);
        OrderPlaced event = createOrderPlacedEvent(orderId, testProductId, 3L);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        inventoryService.reserveOrder(event);
        inventoryService.reserveOrder(event);

        Stock updated = stockRepository
                .findByProductIdAndLocationId(testProductId, testLocationId)
                .orElseThrow();

        assertEquals(3L, updated.getReservedQuantity());
        assertEquals(
                1L,
                inventoryReservationRepository.count()
        );
    }

    private Stock createStock(UUID productId, UUID locationId, long quantity, long reservedQuantity) {
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(quantity);
        stock.setReservedQuantity(reservedQuantity);
        return stockRepository.saveAndFlush(stock);
    }

    private OrderPlaced createOrderPlacedEvent(UUID orderId, UUID productId, long quantity)
    {
        return new OrderPlaced(orderId, List.of(new OrderPlaced.OrderLine(productId, quantity)));
    }
}
