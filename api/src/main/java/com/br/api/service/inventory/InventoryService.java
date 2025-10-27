package com.br.api.service.inventory;

import com.br.api.model.inventory.Inventory;
import com.br.api.repository.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Optional<Inventory> findByCode(String inventoryCode) {
        return inventoryRepository.findByInventoryCode(inventoryCode);
    }

    public List<Inventory> findByName(String name) {
        return inventoryRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Inventory> findByCategory(String category) {
        return inventoryRepository.findByCategory(category);
    }

    public List<Inventory> findByLocation(String location) {
        return inventoryRepository.findByLocation(location);
    }

    public List<Inventory> findByStatus(String status) {
        return inventoryRepository.findByStatus(status);
    }

    @Transactional
    public Inventory createInventory(Inventory inventory) {
        // Gerar código do inventário
        String inventoryCode = "INV-" + System.currentTimeMillis();
        inventory.setInventoryCode(inventoryCode);
        
        // Calcular totais iniciais
        inventory.calculateTotals();
        
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateInventory(Long id, Inventory inventoryDetails) {
        return inventoryRepository.findById(id)
            .map(inventory -> {
                inventory.setName(inventoryDetails.getName());
                inventory.setDescription(inventoryDetails.getDescription());
                inventory.setLocation(inventoryDetails.getLocation());
                inventory.setCategory(inventoryDetails.getCategory());
                inventory.setStatus(inventoryDetails.getStatus());
                
                // Recalcular totais
                inventory.calculateTotals();
                
                return inventoryRepository.save(inventory);
            })
            .orElseThrow(() -> new RuntimeException("Inventário não encontrado com id: " + id));
    }

    @Transactional
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public Object[] getInventorySummary() {
        return inventoryRepository.getInventorySummary();
    }

    public List<Object[]> getInventoryByCategory() {
        return inventoryRepository.getInventoryByCategory();
    }

    public List<Inventory> getHighValueInventories(Double minValue) {
        return inventoryRepository.findByTotalValueGreaterThan(minValue);
    }
}