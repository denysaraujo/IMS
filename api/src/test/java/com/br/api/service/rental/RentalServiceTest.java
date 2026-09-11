package com.br.api.service.rental;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.model.rental.Rental;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.rental.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private RentalService rentalService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(7L);
        customer.setName("Locatário");
        customer.setDocument("12345678909");
    }

    @Test
    void createRental_shouldCalculateTotalAndPersist() {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setProductCode("EQ-01");
        rental.setProductName("Máquina de Lavar");
        rental.setQuantity(1);
        rental.setDailyRate(80.0);
        rental.setRentalDays(3);
        rental.setStartDate(LocalDate.now());

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        InventoryItem inventoryItem = inventoryItem();
        when(inventoryItemRepository.findByProductCode("EQ-01")).thenReturn(Optional.of(inventoryItem));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.createRental(rental);

        assertNotNull(result);
        assertEquals(240.0, result.getTotalAmount());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void returnRental_shouldMarkAsReturned() {
        Rental rental = new Rental();
        rental.setId(10L);
        rental.setStatus("ACTIVE");
        rental.setCustomer(customer);
        rental.setProductCode("EQ-02");
        rental.setProductName("Furadeira");
        rental.setQuantity(1);
        rental.setDailyRate(25.0);
        rental.setRentalDays(2);
        rental.setStartDate(LocalDate.now());

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        InventoryItem inventoryItem = inventoryItem();
        when(inventoryItemRepository.findByProductCode("EQ-02")).thenReturn(Optional.of(inventoryItem));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.returnRental(10L);

        assertEquals("RETURNED", result.getStatus());
        assertNotNull(result.getActualReturnDate());
    }

    private InventoryItem inventoryItem() {
        InventoryItem item = new InventoryItem();
        item.setProductCode("EQ-01");
        item.setProductName("Equipamento");
        item.setCategory("Equipamentos");
        item.setQuantity(10);
        item.setMinStockLevel(1);
        item.setMaxStockLevel(20);
        item.setUnitPrice(100.0);
        return item;
    }
}
