package com.meli.inventory.command.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing an incoming request to process a sale.
 * This object is received via the REST API for the command service.
 */
@Data
public class SaleRequest {

    /**
     * The unique identifier for the product being sold.
     */
    private String productId;

    /**
     * The quantity of the product requested for sale.
     */
    private int quantitySold;
}