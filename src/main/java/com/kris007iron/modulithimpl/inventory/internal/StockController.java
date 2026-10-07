package com.kris007iron.modulithimpl.inventory.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<Stock> addStock(@RequestParam UUID productId, @RequestParam UUID locationId, @RequestParam long quantity) {
        Stock stock = inventoryService.addStock(productId, locationId, quantity);
        return ResponseEntity.ok(stock);
    }

    @PatchMapping
    public ResponseEntity<Stock> removeStock(@RequestParam UUID productId, @RequestParam UUID locationId, @RequestParam long quantity) {
        Stock stock = inventoryService.removeStock(productId, locationId, quantity);
        return ResponseEntity.ok(stock);
    }

    @GetMapping
    public ResponseEntity<Stock> findStock(@RequestParam UUID productId, @RequestParam UUID locationId){
        return inventoryService.findStock(productId, locationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
