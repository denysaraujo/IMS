package com.br.api.service.inventory;

import com.br.api.model.inventory.Inventory;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryItemService(InventoryItemRepository inventoryItemRepository, 
                              InventoryRepository inventoryRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<InventoryItem> findAll() {
        return inventoryItemRepository.findAll();
    }

    public Optional<InventoryItem> findById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    public Optional<InventoryItem> findByProductCode(String productCode) {
        return inventoryItemRepository.findByProductCode(productCode);
    }

    public List<InventoryItem> findByProductName(String productName) {
        return inventoryItemRepository.findByProductNameContainingIgnoreCase(productName);
    }

    public List<InventoryItem> findByCategory(String category) {
        return inventoryItemRepository.findByCategory(category);
    }

    public List<InventoryItem> findByStatus(String status) {
        return inventoryItemRepository.findByStatus(status);
    }

    public List<InventoryItem> findByInventoryId(Long inventoryId) {
        return inventoryItemRepository.findByInventoryId(inventoryId);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems();
    }

    public List<InventoryItem> getOutOfStockItems() {
        return inventoryItemRepository.findOutOfStockItems();
    }

    public List<InventoryItem> getOverStockItems() {
        return inventoryItemRepository.findOverStockItems();
    }

    @Transactional
    public InventoryItem createInventoryItem(Long inventoryId, InventoryItem item) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new RuntimeException("Inventário não encontrado com id: " + inventoryId));
        
        item.setInventory(inventory);
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        // Atualizar totais do inventário
        inventory.calculateTotals();
        inventoryRepository.save(inventory);
        
        return savedItem;
    }

    @Transactional
    public InventoryItem updateInventoryItem(Long id, InventoryItem itemDetails) {
        return inventoryItemRepository.findById(id)
            .map(item -> {
                item.setProductName(itemDetails.getProductName());
                item.setDescription(itemDetails.getDescription());
                item.setCategory(itemDetails.getCategory());
                item.setQuantity(itemDetails.getQuantity());
                item.setMinStockLevel(itemDetails.getMinStockLevel());
                item.setMaxStockLevel(itemDetails.getMaxStockLevel());
                item.setUnitPrice(itemDetails.getUnitPrice());
                item.setSupplier(itemDetails.getSupplier());
                item.setLocation(itemDetails.getLocation());
                
                InventoryItem updatedItem = inventoryItemRepository.save(item);
                
                // Atualizar totais do inventário
                if (item.getInventory() != null) {
                    item.getInventory().calculateTotals();
                    inventoryRepository.save(item.getInventory());
                }
                
                return updatedItem;
            })
            .orElseThrow(() -> new RuntimeException("Item de inventário não encontrado com id: " + id));
    }

    @Transactional
    public InventoryItem updateStock(Long id, Integer quantityChange) {
        return inventoryItemRepository.findById(id)
            .map(item -> {
                if (quantityChange > 0) {
                    item.addStock(quantityChange);
                } else {
                    item.removeStock(Math.abs(quantityChange));
                }
                
                InventoryItem updatedItem = inventoryItemRepository.save(item);
                
                // Atualizar totais do inventário
                if (item.getInventory() != null) {
                    item.getInventory().calculateTotals();
                    inventoryRepository.save(item.getInventory());
                }
                
                return updatedItem;
            })
            .orElseThrow(() -> new RuntimeException("Item de inventário não encontrado com id: " + id));
    }

    @Transactional
    public void deleteInventoryItem(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Item de inventário não encontrado com id: " + id));
        
        Inventory inventory = item.getInventory();
        inventoryItemRepository.deleteById(id);
        
        // Atualizar totais do inventário
        if (inventory != null) {
            inventory.calculateTotals();
            inventoryRepository.save(inventory);
        }
    }

    public List<Object[]> getItemsSummaryByCategory() {
        return inventoryItemRepository.getItemsSummaryByCategory();
    }
}