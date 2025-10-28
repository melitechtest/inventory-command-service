package com.meli.inventory.command.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for communicating stock changes via JMS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateEvent implements Serializable {
    private String productId;
    private int newQuantity;
}
