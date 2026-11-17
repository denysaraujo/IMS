package com.br.api.service.rental;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.Inventory;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.model.rental.Rental;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
import com.br.api.repository.rental.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

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
    void findAll_shouldLoadRentalCustomersForHistory() {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        when(rentalRepository.findAllWithCustomerOrderByCreatedAtDesc()).thenReturn(List.of(rental));

        List<Rental> result = rentalService.findAll();

        assertSame(customer, result.get(0).getCustomer());
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
        rental.setInventoryItemId(1L);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        InventoryItem inventoryItem = inventoryItem();
        when(inventoryItemRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inventoryItem));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.createRental(rental);

        assertNotNull(result);
        assertEquals(240.0, result.getTotalAmount());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(9, inventoryItem.getQuantity());
        assertEquals("Equipamento", result.getProductName());
        verify(inventoryItemRepository).save(inventoryItem);
    }

    @Test
    void createRental_shouldRejectQuantityAboveAvailableStock() {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setInventoryItemId(1L);
        rental.setQuantity(11);
        rental.setDailyRate(80.0);
        rental.setRentalDays(1);
        rental.setStartDate(LocalDate.now());

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        InventoryItem inventoryItem = inventoryItem();
        when(inventoryItemRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(RuntimeException.class, () -> rentalService.createRental(rental));
    }

    @Test
    void createRental_shouldRejectProductNotEnabledForRental() {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setInventoryItemId(1L);
        rental.setQuantity(1);
        rental.setDailyRate(80.0);
        rental.setRentalDays(1);
        rental.setStartDate(LocalDate.now());

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        InventoryItem inventoryItem = inventoryItem();
        inventoryItem.setAvailableForRental(false);
        when(inventoryItemRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(inventoryItem));

        assertThrows(RuntimeException.class, () -> rentalService.createRental(rental));
    }

    @Test
    void createRental_shouldRejectMissingInventoryItem() {
        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setQuantity(1);
        rental.setDailyRate(80.0);
        rental.setRentalDays(1);
        rental.setStartDate(LocalDate.now());

        assertThrows(RuntimeException.class, () -> rentalService.createRental(rental));
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
        InventoryItem inventoryItem = inventoryItem();
        inventoryItem.setProductCode("EQ-02");
        Inventory inventory = new Inventory();
        inventory.addItem(inventoryItem);
        rental.setInventoryItem(inventoryItem);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(rentalRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.returnRental(10L);

        assertEquals("RETURNED", result.getStatus());
        assertNotNull(result.getActualReturnDate());
        assertEquals(11, inventoryItem.getQuantity());
        assertEquals(1100.0, inventory.getTotalValue());
        verify(inventoryItemRepository).save(inventoryItem);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void returnRental_shouldNotRestoreStockMoreThanOnce() {
        Rental rental = new Rental();
        rental.setId(10L);
        rental.setStatus("RETURNED");
        when(rentalRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(rental));

        assertThrows(RuntimeException.class, () -> rentalService.returnRental(10L));
    }

    @Test
    void deleteRental_shouldRemovePendingRentalAndRestoreStock() {
        Rental rental = new Rental();
        rental.setId(11L);
        rental.setStatus("ACTIVE");
        rental.setCustomer(customer);
        rental.setProductCode("EQ-03");
        rental.setProductName("Trator");
        rental.setQuantity(2);
        rental.setDailyRate(50.0);
        rental.setRentalDays(3);
        rental.setStartDate(LocalDate.now());
        InventoryItem inventoryItem = inventoryItem();
        inventoryItem.setProductCode("EQ-03");
        inventoryItem.setQuantity(8);
        inventoryItem.setAvailableForRental(true);
        Inventory inventory = new Inventory();
        inventory.addItem(inventoryItem);
        rental.setInventoryItem(inventoryItem);

        when(rentalRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(rental));

        rentalService.deleteRental(11L);

        assertEquals(10, inventoryItem.getQuantity());
        verify(inventoryItemRepository).save(inventoryItem);
        verify(inventoryRepository).save(inventory);
        verify(rentalRepository).delete(rental);
    }

    private InventoryItem inventoryItem() {
        InventoryItem item = new InventoryItem();
        item.setProductCode("EQ-01");
        item.setProductName("Equipamento");
        item.setCategory("Equipamentos");
        item.setQuantity(10);
        item.setAvailableForRental(true);
        item.setMinStockLevel(1);
        item.setMaxStockLevel(20);
        item.setUnitPrice(100.0);
        return item;
    }
}
