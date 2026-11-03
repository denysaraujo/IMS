package com.br.api.service.inventory;

import com.br.api.model.inventory.Inventory;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryItemService inventoryItemService;

    @Test
    void createInventoryItem_shouldAssociateItemAndUpdateInventoryTotals() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);

        InventoryItem item = new InventoryItem();
        item.setProductName("Cadeira");
        item.setCategory("Móveis");
        item.setQuantity(3);
        item.setMinStockLevel(1);
        item.setMaxStockLevel(10);
        item.setUnitPrice(120.0);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryItemRepository.save(any(InventoryItem.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem createdItem = inventoryItemService.createInventoryItem(1L, item);

        assertSame(inventory, createdItem.getInventory());
        assertEquals(1, inventory.getItems().size());
        assertEquals(1, inventory.getTotalItems());
        assertEquals(360.0, inventory.getTotalValue());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void inventoryItem_shouldDeserializeExpirationDateFromJson() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

        InventoryItem item = objectMapper.readValue(
            "{\"expirationDate\":\"2099-12-31\"}",
            InventoryItem.class
        );

        assertEquals(LocalDate.of(2099, 12, 31), item.getExpirationDate());
    }

    @Test
    void updateInventoryItem_shouldUpdateExpirationAndMoveItemBetweenInventories() {
        Inventory originalInventory = new Inventory();
        originalInventory.setId(1L);
        Inventory targetInventory = new Inventory();
        targetInventory.setId(2L);

        InventoryItem existingItem = new InventoryItem();
        existingItem.setProductName("Cadeira");
        existingItem.setCategory("Móveis");
        existingItem.setQuantity(2);
        existingItem.setMinStockLevel(1);
        existingItem.setMaxStockLevel(10);
        existingItem.setUnitPrice(100.0);
        existingItem.setExpirationDate(LocalDate.of(2028, 1, 1));
        originalInventory.addItem(existingItem);

        InventoryItem update = new InventoryItem();
        update.setProductName("Cadeira executiva");
        update.setCategory("Móveis");
        update.setQuantity(3);
        update.setMinStockLevel(1);
        update.setMaxStockLevel(10);
        update.setUnitPrice(120.0);
        update.setExpirationDate(LocalDate.of(2030, 6, 15));
        update.setInventoryId(2L);

        when(inventoryItemRepository.findById(7L)).thenReturn(Optional.of(existingItem));
        when(inventoryRepository.findById(2L)).thenReturn(Optional.of(targetInventory));
        when(inventoryItemRepository.save(any(InventoryItem.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem saved = inventoryItemService.updateInventoryItem(7L, update);

        assertSame(targetInventory, saved.getInventory());
        assertEquals(0, originalInventory.getTotalItems());
        assertEquals(1, targetInventory.getTotalItems());
        assertEquals(360.0, targetInventory.getTotalValue());
        assertEquals(LocalDate.of(2030, 6, 15), saved.getExpirationDate());
        verify(inventoryRepository).save(originalInventory);
        verify(inventoryRepository).save(targetInventory);
    }

    @Test
    void inventoryItem_shouldExposeItsInventoryIdInJson() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
        Inventory inventory = new Inventory();
        inventory.setId(12L);
        InventoryItem item = new InventoryItem();
        item.setQuantity(1);
        item.setUnitPrice(10.0);
        item.setInventory(inventory);

        String json = objectMapper.writeValueAsString(item);

        assertTrue(json.contains("\"inventoryId\":12"));
    }

    @Test
    void inventoryItem_shouldReadItsInventoryIdFromJson() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

        InventoryItem item = objectMapper.readValue("{\"inventoryId\":12}", InventoryItem.class);

        assertEquals(12L, item.getInventoryId());
    }

    @Test
    void deleteInventoryItem_shouldUpdateInventoryTotals() {
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        InventoryItem item = new InventoryItem();
        item.setProductName("Cadeira");
        item.setCategory("Móveis");
        item.setQuantity(2);
        item.setMinStockLevel(1);
        item.setMaxStockLevel(10);
        item.setUnitPrice(100.0);
        inventory.addItem(item);

        when(inventoryItemRepository.findById(7L)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(Inventory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        inventoryItemService.deleteInventoryItem(7L);

        assertTrue(inventory.getItems().isEmpty());
        assertEquals(0, inventory.getTotalItems());
        assertEquals(0.0, inventory.getTotalValue());
        verify(inventoryItemRepository).delete(item);
        verify(inventoryRepository).save(inventory);
    }
}
