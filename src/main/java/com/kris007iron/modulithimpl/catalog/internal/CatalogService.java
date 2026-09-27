package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;

    public ProductReference addProduct(ProductDTO productDTO){
        // optional sku validation with provided specification
        Optional<Product> product = productRepository.findBySku(productDTO.sku());
        if(product.isEmpty()){
            Product newProduct = new Product();
            newProduct.setSku(productDTO.sku());
            newProduct.setName(productDTO.name());
            newProduct.setDescription(productDTO.description());
            newProduct.setPrice(productDTO.price());
            newProduct.setCurrency(productDTO.currency());
            productRepository.save(newProduct);
            return new ProductReference(newProduct.getId(), newProduct.getSku());
        }else{
            return new ProductReference(product.get().getId(), product.get().getSku());
        }
    }

}
