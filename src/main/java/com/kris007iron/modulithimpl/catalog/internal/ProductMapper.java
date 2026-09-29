package com.kris007iron.modulithimpl.catalog.internal;

import com.kris007iron.modulithimpl.catalog.ProductReference;

public final class ProductMapper {
    public static ProductReference toReference(Product product){
        return new ProductReference(product.getId(), product.getSku());
    }
    public static Product fromDto(ProductDTO productDTO){
        Product product = new Product();
        product.setSku(productDTO.sku());
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setPrice(productDTO.price());
        product.setCurrency(productDTO.currency());
        return product;
    }
}
