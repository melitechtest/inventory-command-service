package com.meli.inventory.command.controller;

import com.meli.inventory.command.dto.SaleRequest;
import com.meli.inventory.command.model.InventoryItem;
import com.meli.inventory.command.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling inventory write commands.
 */
@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
public class InventoryCommandController {

    private final InventoryService inventoryService;

    @PostMapping("/sale")
    public ResponseEntity<?> processSale(@RequestBody SaleRequest saleRequest) {
        try {
            InventoryItem updatedItem = inventoryService.handleSale(saleRequest);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/restock")
    public ResponseEntity<InventoryItem> processRestock(@RequestParam String productId, @RequestParam int quantity) {
        InventoryItem updatedItem = inventoryService.handleRestock(productId, quantity);
        return ResponseEntity.ok(updatedItem);
    }
}