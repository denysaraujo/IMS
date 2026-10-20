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
}
