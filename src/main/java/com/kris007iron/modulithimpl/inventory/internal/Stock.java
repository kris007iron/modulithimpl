package com.kris007iron.modulithimpl.inventory.internal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"productId", "locationId"}
        )
)
public class Stock {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID productId;
    private UUID locationId;
    private Long quantity;
    private Long reservedQuantity;

}
