package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.inventory.internal.exception.StockNotFoundException;
import com.kris007iron.modulithimpl.orders.OrderPlaced;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final StockRepository stockRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    public Stock addStock(UUID productId, UUID locationId, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Stock stock = stockRepository
                .findByProductIdAndLocationId(productId, locationId)
                .orElseGet(() -> createStock(productId, locationId));

        stock.setQuantity(stock.getQuantity() + quantity);
        return stockRepository.save(stock);
    }

    public Stock removeStock(UUID productId, UUID locationId, long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Stock stock = stockRepository
                .findByProductIdAndLocationId(productId, locationId)
                .orElseThrow(() -> new StockNotFoundException(productId, locationId));

        if (stock.getQuantity() - quantity < 0) {
            throw new InsufficientStockException(productId, locationId);
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        return stockRepository.save(stock);
    }

    public Optional<Stock> findStock(UUID productId, UUID locationId) {
        return stockRepository.findByProductIdAndLocationId(productId, locationId);
    }

    private long available(Stock stock) {
        long quantity = stock.getQuantity() == null ? 0 : stock.getQuantity();
        long reservedQuantity = stock.getReservedQuantity() == null ? 0 : stock.getReservedQuantity();
        return quantity - reservedQuantity;
    }

    private Stock createStock(UUID productId, UUID locationId) {
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(0L);
        stock.setReservedQuantity(0L);
        return stock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveOrder(OrderPlaced event) {
        int inserted = inventoryReservationRepository.tryCreateReservation(
                event.orderId(),
                ReservationStatus.RESERVED.name()
        );

        if (inserted == 0) {
            return;
        }
        Map<UUID, Long> requiredQuantities = event.items().stream()
                .collect(Collectors.groupingBy(
                        OrderPlaced.OrderLine::productId,
                        Collectors.summingLong(OrderPlaced.OrderLine::quantity)
                ));
        List<Stock> stocks = stockRepository.findByProductIdInOrderByLocationIdAsc(requiredQuantities.keySet());
        Map<UUID, List<Stock>> stocksByLocation = stocks.stream().collect(Collectors.groupingBy(Stock::getLocationId));
        Map.Entry<UUID, List<Stock>> selectedLocation =
                stocksByLocation.entrySet().stream()
                        .filter(entry -> requiredQuantities.entrySet().stream()
                                .allMatch(required -> entry.getValue().stream()
                                        .filter(stock -> stock.getProductId()
                                                .equals(required.getKey()))
                                        .anyMatch(stock -> available(stock)
                                                >= required.getValue())))
                        .findFirst()
                        .orElseThrow(() ->
                        {
                            return new InsufficientStockException(event.orderId());
                        });

        UUID locationId = selectedLocation.getKey();

        Map<UUID, Stock> selectedStocks = selectedLocation.getValue()
                .stream()
                .collect(Collectors.toMap(
                        Stock::getProductId,
                        Function.identity()
                ));

        requiredQuantities.forEach((productId, quantity) -> {
            Stock stock = selectedStocks.get(productId);
            stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        });

        InventoryReservation reservation =
                inventoryReservationRepository.findById(event.orderId())
                        .orElseThrow();

        reservation.setLocationId(locationId);
    }
}
