package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductAlreadyExistsException;
import com.kris007iron.modulithimpl.catalog.internal.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;

    public ProductReference addProduct(ProductDTO productDTO){
        // optional sku validation with provided specification
        if (productRepository.findBySku(productDTO.sku()).isPresent()) {
            throw new ProductAlreadyExistsException(productDTO.sku());
        }

        Product product = productRepository.save(ProductMapper.fromDto(productDTO));
        return ProductMapper.toReference(product);
    }

    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        productRepository.deleteById(productId);
    }

    public Optional<ProductReference> findProduct(UUID productId) {
        return productRepository.findById(productId).map(ProductMapper::toReference);
    }

    public Optional<ProductReference> findProductBySku(String sku) {
        return productRepository.findBySku(sku).map(ProductMapper::toReference);
    }
}
