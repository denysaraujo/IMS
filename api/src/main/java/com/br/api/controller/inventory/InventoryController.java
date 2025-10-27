package com.br.api.controller.inventory;

import com.br.api.model.inventory.Inventory;
import com.br.api.service.inventory.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Inventory> getAllInventories() {
        return inventoryService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Optional<Inventory> inventory = inventoryService.findById(id);
        return inventory.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{inventoryCode}")
    public ResponseEntity<Inventory> getInventoryByCode(@PathVariable String inventoryCode) {
        Optional<Inventory> inventory = inventoryService.findByCode(inventoryCode);
        return inventory.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name")
    public List<Inventory> getInventoriesByName(@RequestParam String name) {
        return inventoryService.findByName(name);
    }

    @GetMapping("/search/category")
    public List<Inventory> getInventoriesByCategory(@RequestParam String category) {
        return inventoryService.findByCategory(category);
    }

    @GetMapping("/search/location")
    public List<Inventory> getInventoriesByLocation(@RequestParam String location) {
        return inventoryService.findByLocation(location);
    }

    @GetMapping("/status/{status}")
    public List<Inventory> getInventoriesByStatus(@PathVariable String status) {
        return inventoryService.findByStatus(status);
    }

    @PostMapping
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventoryDetails) {
        try {
            Inventory updatedInventory = inventoryService.updateInventory(id, inventoryDetails);
            return ResponseEntity.ok(updatedInventory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Object[]> getInventorySummary() {
        Object[] summary = inventoryService.getInventorySummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<Object[]>> getInventoryByCategory() {
        List<Object[]> categorySummary = inventoryService.getInventoryByCategory();
        return ResponseEntity.ok(categorySummary);
    }

    @GetMapping("/high-value")
    public List<Inventory> getHighValueInventories(@RequestParam Double minValue) {
        return inventoryService.getHighValueInventories(minValue);
    }
}