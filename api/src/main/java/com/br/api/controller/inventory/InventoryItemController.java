package com.br.api.controller.inventory;

import com.br.api.model.inventory.InventoryItem;
import com.br.api.service.inventory.InventoryItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory/items")
@CrossOrigin(origins = "*")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    public InventoryItemController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @GetMapping
    public List<InventoryItem> getAllItems() {
        return inventoryItemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItemById(@PathVariable Long id) {
        Optional<InventoryItem> item = inventoryItemService.findById(id);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<InventoryItem> getItemByCode(@PathVariable String productCode) {
        Optional<InventoryItem> item = inventoryItemService.findByProductCode(productCode);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name")
    public List<InventoryItem> getItemsByName(@RequestParam String productName) {
        return inventoryItemService.findByProductName(productName);
    }

    @GetMapping("/search/category")
    public List<InventoryItem> getItemsByCategory(@RequestParam String category) {
        return inventoryItemService.findByCategory(category);
    }

    @GetMapping("/status/{status}")
    public List<InventoryItem> getItemsByStatus(@PathVariable String status) {
        return inventoryItemService.findByStatus(status);
    }

    @GetMapping("/inventory/{inventoryId}")
    public List<InventoryItem> getItemsByInventory(@PathVariable Long inventoryId) {
        return inventoryItemService.findByInventoryId(inventoryId);
    }

    @GetMapping("/low-stock")
    public List<InventoryItem> getLowStockItems() {
        return inventoryItemService.getLowStockItems();
    }

    @GetMapping("/out-of-stock")
    public List<InventoryItem> getOutOfStockItems() {
        return inventoryItemService.getOutOfStockItems();
    }

    @GetMapping("/over-stock")
    public List<InventoryItem> getOverStockItems() {
        return inventoryItemService.getOverStockItems();
    }

    @PostMapping("/inventory/{inventoryId}")
    public ResponseEntity<InventoryItem> createItem(@PathVariable Long inventoryId, @RequestBody InventoryItem item) {
        try {
            InventoryItem createdItem = inventoryItemService.createInventoryItem(inventoryId, item);
            return ResponseEntity.ok(createdItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem itemDetails) {
        try {
            InventoryItem updatedItem = inventoryItemService.updateInventoryItem(id, itemDetails);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<InventoryItem> updateStock(@PathVariable Long id, @RequestParam Integer quantityChange) {
        try {
            InventoryItem updatedItem = inventoryItemService.updateStock(id, quantityChange);
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            inventoryItemService.deleteInventoryItem(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<Object[]>> getItemsSummaryByCategory() {
        List<Object[]> summary = inventoryItemService.getItemsSummaryByCategory();
        return ResponseEntity.ok(summary);
    }
}