package com.meli.inventory.command.service;

import com.meli.inventory.command.dto.SaleRequest;
import com.meli.inventory.command.dto.StockUpdateEvent;
import com.meli.inventory.command.model.InventoryItem;
import com.meli.inventory.command.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/**
 * Handles all write operations (sales and restock) and publishes events using JMS/Artemis.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final JmsTemplate jmsTemplate;

    private static final String STOCK_TOPIC = "stock.update.topic";

    /**
     * Handles sale by locking the row and subtracting quantity. Publishes event after commit.
     */
    @Transactional
    public InventoryItem handleSale(SaleRequest saleRequest) {
        String productId = saleRequest.getProductId();

        InventoryItem item = inventoryRepository.findByProductIdWithLock(productId).orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (item.getQuantity() < saleRequest.getQuantitySold()) {
            throw new RuntimeException("Insufficient stock for product " + productId);
        }

        item.setQuantity(item.getQuantity() - saleRequest.getQuantitySold());
        InventoryItem updated = inventoryRepository.save(item);

        publishStockUpdateAfterCommit(updated);
        return updated;
    }

    /**
     * Handles restock with safe create-or-update semantics. Publishes event after commit.
     */
    @Transactional
    public InventoryItem handleRestock(String productId, int quantityAdded) {
        if (quantityAdded <= 0) {
            throw new IllegalArgumentException("quantityAdded must be > 0");
        }

        Optional<InventoryItem> maybe = inventoryRepository.findByProductIdWithLock(productId);

        if (maybe.isPresent()) {
            InventoryItem item = maybe.get();
            item.setQuantity(item.getQuantity() + quantityAdded);
            InventoryItem updated = inventoryRepository.save(item);
            publishStockUpdateAfterCommit(updated);
            return updated;
        }

        InventoryItem newItem = new InventoryItem();
        newItem.setProductId(productId);
        newItem.setQuantity(quantityAdded);

        try {
            InventoryItem saved = inventoryRepository.save(newItem);
            publishStockUpdateAfterCommit(saved);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            InventoryItem item = inventoryRepository.findByProductIdWithLock(productId).orElseThrow(() -> new RuntimeException("Failed to find product after concurrent insert: " + productId));
            item.setQuantity(item.getQuantity() + quantityAdded);
            InventoryItem updated = inventoryRepository.save(item);
            publishStockUpdateAfterCommit(updated);
            return updated;
        }
    }

    /**
     * Enqueue JMS publish to happen after the surrounding DB transaction commits.
     * This avoids the race where a listener sees the event before DB/Redis are updated.
     */
    private void publishStockUpdateAfterCommit(InventoryItem item) {
        StockUpdateEvent event = new StockUpdateEvent(item.getProductId(), item.getQuantity());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    jmsTemplate.convertAndSend(STOCK_TOPIC, event);
                }
            });
        } else {
            jmsTemplate.convertAndSend(STOCK_TOPIC, event);
        }
    }
}