package com.meli.inventory.command.service;

import com.meli.inventory.command.config.RabbitMQConfig;
import com.meli.inventory.command.dto.SaleRequest;
import com.meli.inventory.command.dto.StockUpdateEvent;
import com.meli.inventory.command.model.InventoryItem;
import com.meli.inventory.command.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles all write operations (sales and restock) and publishes events.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RabbitTemplate rabbitTemplate;

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
        InventoryItem item = inventoryRepository.findByProductId(productId).orElseGet(() -> {
            InventoryItem newItem = new InventoryItem();
            newItem.setProductId(productId);
            return newItem;
        });

        item.setQuantity(item.getQuantity() + quantityAdded);
        InventoryItem updatedItem = inventoryRepository.save(item);

        publishStockUpdate(updatedItem);
        return updatedItem;
    }

    /**
     * Publishes a stock update event to RabbitMQ.
     */
    private void publishStockUpdate(InventoryItem item) {
        StockUpdateEvent event = new StockUpdateEvent(item.getProductId(), item.getQuantity());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
    }
}