package com.Inventory.productsheet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Inventory.productsheet.model.InventoryItem;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    // Only active items (soft delete)
    List<InventoryItem> findByDeletedFalse();
}