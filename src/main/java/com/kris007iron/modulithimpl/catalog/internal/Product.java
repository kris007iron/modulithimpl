package com.kris007iron.modulithimpl.catalog.internal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true, nullable = false)
    private String sku;
    private String name;
    @Column(length = 2000)
    private String description;
    private BigDecimal price;
    private Currency currency;
}
