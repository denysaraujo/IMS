package com.br.api.repository.inventory;

import com.br.api.model.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByInventoryCode(String inventoryCode);
    
    List<Inventory> findByNameContainingIgnoreCase(String name);
    
    List<Inventory> findByCategory(String category);
    
    List<Inventory> findByLocation(String location);
    
    List<Inventory> findByStatus(String status);
    
    @Query("SELECT i FROM Inventory i WHERE i.totalValue > :minValue")
    List<Inventory> findByTotalValueGreaterThan(@Param("minValue") Double minValue);
    
    @Query("SELECT COUNT(i), SUM(i.totalValue), SUM(i.totalItems) FROM Inventory i")
    Object[] getInventorySummary();
    
    @Query("SELECT i.category, COUNT(i), SUM(i.totalValue) FROM Inventory i GROUP BY i.category")
    List<Object[]> getInventoryByCategory();
}