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
     * Non-locking read by productId. Use when you only need to read current state.
     */
    Optional<InventoryItem> findByProductId(String productId);

    /**
     * Read by productId with a PESSIMISTIC_WRITE lock.
     * Call this from a method annotated with @Transactional so the lock is actually applied.
     * This prevents concurrent transactions from inserting/updating the same product simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findByProductIdForUpdate(String productId);
}