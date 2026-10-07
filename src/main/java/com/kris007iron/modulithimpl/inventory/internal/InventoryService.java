package com.kris007iron.modulithimpl.inventory.internal;

import com.kris007iron.modulithimpl.inventory.internal.exception.InsufficientStockException;
import com.kris007iron.modulithimpl.inventory.internal.exception.StockNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final StockRepository stockRepository;

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

    private Stock createStock(UUID productId, UUID locationId) {
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setLocationId(locationId);
        stock.setQuantity(0L);
        stock.setReservedQuantity(0L);
        return stock;
    }
}
