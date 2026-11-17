package com.br.api.service.sales;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.Inventory;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.model.sales.Sale;
import com.br.api.model.sales.SaleItem;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
import com.br.api.repository.sales.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private SaleService saleService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Cliente Teste");
        customer.setDocument("12345678900");
    }

    @Test
    void processSale_shouldDecreaseInventoryAndSaveSale() {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setSaleCode("V-1001");

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(10L);
        inventoryItem.setProductCode(null);
        inventoryItem.setProductName("Teclado");
        inventoryItem.setDescription("Teclado mecânico");
        inventoryItem.setCategory("Periféricos");
        inventoryItem.setQuantity(5);
        inventoryItem.setMinStockLevel(2);
        inventoryItem.setMaxStockLevel(20);
        inventoryItem.setUnitPrice(200.0);
        Inventory inventory = new Inventory();
        inventory.setId(3L);
        inventory.addItem(inventoryItem);

        SaleItem saleItem = new SaleItem(null, "Nome informado pelo cliente", 2, 175.0);
        saleItem.setInventoryItemId(10L);
        saleItem.setInventoryItem(inventoryItem);
        sale.setItems(List.of(saleItem));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(inventoryItem));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sale result = saleService.processSale(sale);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(3, inventoryItem.getQuantity());
        assertEquals(350.0, result.getTotalAmount());
        assertEquals("Teclado", saleItem.getProductName());
        assertEquals("Periféricos", saleItem.getCategory());
        assertEquals("Teclado mecânico", saleItem.getDescription());
        assertEquals(10L, saleItem.getInventoryItemId());
        assertSame(sale, saleItem.getSale());
        assertEquals(1, inventory.getTotalItems());
        assertEquals(600.0, inventory.getTotalValue());
        verify(inventoryItemRepository, times(1)).save(inventoryItem);
        verify(inventoryRepository).save(inventory);
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void processSale_shouldRejectQuantityGreaterThanAvailableStock() {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        SaleItem saleItem = new SaleItem(null, "Produto", 4, 10.0);
        saleItem.setInventoryItemId(10L);
        sale.setItems(List.of(saleItem));

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(10L);
        inventoryItem.setProductName("Produto");
        inventoryItem.setCategory("Categoria");
        inventoryItem.setQuantity(3);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(RuntimeException.class, () -> saleService.processSale(sale));

        assertEquals(3, inventoryItem.getQuantity());
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void cancelSale_shouldRestoreLinkedInventoryItemOnce() {
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(10L);
        inventoryItem.setProductName("Teclado");
        inventoryItem.setQuantity(3);
        SaleItem saleItem = new SaleItem(null, "Teclado", 2, 175.0);
        saleItem.setInventoryItem(inventoryItem);
        Sale sale = new Sale();
        sale.addItem(saleItem);
        sale.setStatus("COMPLETED");

        when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));
        when(inventoryItemRepository.save(any(InventoryItem.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        saleService.cancelSale(5L);

        assertEquals("CANCELLED", sale.getStatus());
        assertEquals(5, inventoryItem.getQuantity());
        assertThrows(RuntimeException.class, () -> saleService.cancelSale(5L));
        assertEquals(5, inventoryItem.getQuantity());
        verify(inventoryItemRepository).save(inventoryItem);
        verify(saleRepository).save(sale);
    }

    @Test
    void processSale_shouldRequireAnExistingCustomerBeforeChangingStock() {
        Sale sale = new Sale();
        sale.setItems(List.of(new SaleItem(null, "Produto", 1, 10.0)));

        assertThrows(RuntimeException.class, () -> saleService.processSale(sale));

        verifyNoInteractions(customerRepository, inventoryItemRepository, saleRepository);
    }

    @Test
    void processSale_shouldNotChangeStockWhenCustomerDoesNotExist() {
        Sale sale = new Sale();
        Customer unknownCustomer = new Customer();
        unknownCustomer.setId(99L);
        sale.setCustomer(unknownCustomer);
        sale.setItems(List.of(new SaleItem(null, "Produto", 1, 10.0)));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> saleService.processSale(sale));

        verifyNoInteractions(inventoryItemRepository, saleRepository);
    }

    @Test
    void cancelSale_shouldNotRestoreStockForAnUnprocessedSale() {
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setProductName("Teclado");
        inventoryItem.setQuantity(3);
        SaleItem saleItem = new SaleItem(null, "Teclado", 2, 175.0);
        saleItem.setInventoryItem(inventoryItem);
        Sale sale = new Sale();
        sale.addItem(saleItem);
        sale.setStatus("PENDING");
        when(saleRepository.findById(5L)).thenReturn(Optional.of(sale));

        assertThrows(RuntimeException.class, () -> saleService.cancelSale(5L));

        assertEquals(3, inventoryItem.getQuantity());
        verify(saleRepository, never()).save(any(Sale.class));
        verifyNoInteractions(inventoryItemRepository);
    }

    @Test
    void deleteSale_shouldRestoreCompletedSaleStockAndRemoveRecord() {
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(15L);
        inventoryItem.setProductName("Monitor");
        inventoryItem.setQuantity(4);
        inventoryItem.setUnitPrice(500.0);
        Inventory inventory = new Inventory();
        inventory.setId(22L);
        inventory.addItem(inventoryItem);

        SaleItem saleItem = new SaleItem(null, "Monitor", 2, 250.0);
        saleItem.setInventoryItem(inventoryItem);

        Sale sale = new Sale();
        sale.addItem(saleItem);
        sale.setStatus("COMPLETED");

        when(saleRepository.findById(25L)).thenReturn(Optional.of(sale));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        saleService.deleteSale(25L);

        assertEquals(6, inventoryItem.getQuantity());
        verify(inventoryItemRepository).save(inventoryItem);
        verify(inventoryRepository).save(inventory);
        verify(saleRepository).delete(sale);
    }
}
