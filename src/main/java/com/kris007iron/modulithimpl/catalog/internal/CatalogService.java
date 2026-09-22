package com.kris007iron.modulithimpl.catalog.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;

}
