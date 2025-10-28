package com.meli.inventory.command.service;

import com.meli.inventory.command.dto.SaleRequest;
import com.meli.inventory.command.dto.StockUpdateEvent;
import com.meli.inventory.command.model.InventoryItem;
import com.meli.inventory.command.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jms.core.JmsTemplate;

/**
 * Handles all write operations (sales and restock) and publishes events using JMS/Artemis.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final JmsTemplate jmsTemplate;

    private static final String STOCK_TOPIC = "stock.update.topic";

    @Transactional
    public InventoryItem handleSale(SaleRequest saleRequest) {
        InventoryItem item = inventoryRepository.findByProductId(saleRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: " + saleRequest.getProductId()));

        if (item.getQuantity() < saleRequest.getQuantitySold()) {
            throw new RuntimeException("Insufficient stock for product " + saleRequest.getProductId());
        }

        item.setQuantity(item.getQuantity() - saleRequest.getQuantitySold());
        InventoryItem updatedItem = inventoryRepository.save(item);

        publishStockUpdate(updatedItem);
        return updatedItem;
    }

    @Transactional
    public InventoryItem handleRestock(String productId, int quantityAdded) {
        if (quantityAdded <= 0) {
            throw new IllegalArgumentException("quantityAdded must be > 0");
        }

        InventoryItem item = inventoryRepository.findByProductId(productId).orElseGet(() -> {
            InventoryItem newItem = new InventoryItem();
            newItem.setProductId(productId);
            newItem.setQuantity(0);
            return newItem;
        });

        int current = item.getQuantity();
        item.setQuantity(current + quantityAdded);

        InventoryItem updatedItem = inventoryRepository.save(item);

        try {
            publishStockUpdate(updatedItem);
        } catch (org.springframework.jms.JmsSecurityException ex) {
            throw new RuntimeException("Failed to publish stock update to broker: " + ex.getMessage(), ex);
        }

        return updatedItem;
    }

    /**
     * Publishes a stock update event to the Artemis Topic using JmsTemplate.
     */
    private void publishStockUpdate(InventoryItem item) {
        StockUpdateEvent event = new StockUpdateEvent(item.getProductId(), item.getQuantity());

        jmsTemplate.convertAndSend(STOCK_TOPIC, event);
    }
}