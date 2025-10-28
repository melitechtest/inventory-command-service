package com.meli.inventory.command.repository;

import com.meli.inventory.command.model.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * Repository for accessing InventoryItem data.
 * Uses pessimistic lock for thread-safe inventory updates.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Finds an item by ID, applying a PESSIMISTIC_WRITE lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findByProductId(String productId);
}