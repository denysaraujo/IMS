package com.br.api.repository.inventory;

import com.br.api.model.inventory.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    Optional<InventoryItem> findByProductCode(String productCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.id = :id")
    Optional<InventoryItem> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.productCode = :productCode")
    Optional<InventoryItem> findByProductCodeForUpdate(@Param("productCode") String productCode);
    
    List<InventoryItem> findByProductNameContainingIgnoreCase(String productName);
    
    List<InventoryItem> findByCategory(String category);
    
    List<InventoryItem> findByStatus(String status);
    
    List<InventoryItem> findBySupplier(String supplier);
    
    List<InventoryItem> findByInventory_Id(Long inventoryId);

    List<InventoryItem> findByLowStockAlertTrue();
    
    List<InventoryItem> findByExpirationDateBeforeAndExpirationDateNotNull(LocalDate date);
    
    Long countByLowStockAlertTrue();
    
    Long countByExpirationDateBefore(LocalDate date);
        
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minStockLevel")
    List<InventoryItem> findLowStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity = 0")
    List<InventoryItem> findOutOfStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.quantity > i.maxStockLevel")
    List<InventoryItem> findOverStockItems();
    
    @Query("SELECT i.category, COUNT(i), SUM(i.quantity), SUM(i.quantity * i.unitPrice) FROM InventoryItem i GROUP BY i.category")
    List<Object[]> getItemsSummaryByCategory();

    @Query(value = "SELECT company_id, COUNT(*) FROM inventory_items GROUP BY company_id", nativeQuery = true)
    List<Object[]> countAllByCompany();
}