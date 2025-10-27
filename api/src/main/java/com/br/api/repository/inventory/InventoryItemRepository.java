package com.br.api.repository.inventory;

import com.br.api.model.inventory.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findByProductCode(String productCode);
    
    List<InventoryItem> findByProductNameContainingIgnoreCase(String productName);
    
    List<InventoryItem> findByCategory(String category);
    
    List<InventoryItem> findByStatus(String status);
    
    List<InventoryItem> findBySupplier(String supplier);
    
    List<InventoryItem> findByInventoryId(Long inventoryId);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minStockLevel")
    List<InventoryItem> findLowStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0")
    List<InventoryItem> findOutOfStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity > i.maxStockLevel")
    List<InventoryItem> findOverStockItems();
    
    @Query("SELECT i.category, COUNT(i), SUM(i.quantity), SUM(i.quantity * i.unitPrice) FROM InventoryItem i GROUP BY i.category")
    List<Object[]> getItemsSummaryByCategory();
}