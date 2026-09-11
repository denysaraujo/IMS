package com.br.api.service.sales;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.model.sales.Sale;
import com.br.api.model.sales.SaleItem;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

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
        inventoryItem.setProductCode("P-001");
        inventoryItem.setProductName("Teclado");
        inventoryItem.setCategory("Periféricos");
        inventoryItem.setQuantity(5);
        inventoryItem.setMinStockLevel(2);
        inventoryItem.setMaxStockLevel(20);
        inventoryItem.setUnitPrice(200.0);

        SaleItem saleItem = new SaleItem("P-001", "Teclado", 2, 200.0);
        saleItem.setInventoryItem(inventoryItem);
        sale.setItems(List.of(saleItem));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(inventoryItemRepository.findByProductCode("P-001")).thenReturn(Optional.of(inventoryItem));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sale result = saleService.processSale(sale);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(3, inventoryItem.getQuantity());
        verify(inventoryItemRepository, times(1)).save(inventoryItem);
        verify(saleRepository, times(1)).save(any(Sale.class));
    }
}
