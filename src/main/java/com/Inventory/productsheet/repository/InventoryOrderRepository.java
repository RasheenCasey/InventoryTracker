package com.Inventory.productsheet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Inventory.productsheet.model.InventoryOrder;

@Repository
public interface InventoryOrderRepository extends JpaRepository<InventoryOrder, Long> {
    
    /**
     * Find orders by item ID and status
     */
    List<InventoryOrder> findByItemIdAndStatus(Long itemId, String status);
    
    /**
     * Find orders by status
     */
    List<InventoryOrder> findByStatus(String status);
    
    /**
     * Find orders by item ID
     */
    List<InventoryOrder> findByItemId(Long itemId);
    
    /**
     * Find pending orders
     */
    List<InventoryOrder> findByStatusOrderByOrderDateDesc(String status);
}
