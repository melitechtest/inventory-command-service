package com.meli.inventory.command.repository;

import com.meli.inventory.command.model.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for accessing InventoryItem data.
 * Uses pessimistic lock for thread-safe inventory updates.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Non-locking read by productId. Use when you only need to read current state.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
    Optional<InventoryItem> findByProductId(@Param("productId") String productId);

    /**
     * Read by productId with a PESSIMISTIC_WRITE lock.
     * Call this from a method annotated with @Transactional so the lock is actually applied.
     * This prevents concurrent transactions from inserting/updating the same product simultaneously.
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findByProductIdWithLock(@Param("productId") String productId);
}