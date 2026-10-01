package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final CatalogService catalogService;

    @PostMapping
    public ResponseEntity<ProductReference> addProduct(@RequestBody ProductDTO productDTO) {
        ProductReference productReference = catalogService.addProduct(productDTO);
        return ResponseEntity.ok(productReference);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductReference> findProduct(@PathVariable UUID productId) {
        return catalogService.findProduct(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public  ResponseEntity<ProductReference> findProductBySku(@RequestParam String sku) {
        return catalogService.findProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{productId}")
    public  ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        catalogService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}
