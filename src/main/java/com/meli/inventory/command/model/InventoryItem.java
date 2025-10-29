package com.meli.inventory.command.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the single source of truth for inventory items in the PostgreSQL database.
 */
@Entity
@Table(name = "inventory_items", uniqueConstraints = @UniqueConstraint(columnNames = "productId"))
@Data
@NoArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String productId;

    private int quantity;
}