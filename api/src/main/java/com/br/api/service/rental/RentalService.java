package com.br.api.service.rental;

import com.br.api.model.customer.Customer;
import com.br.api.model.rental.Rental;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.rental.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public RentalService(RentalRepository rentalRepository, CustomerRepository customerRepository,
                         InventoryItemRepository inventoryItemRepository) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public Optional<Rental> findById(Long id) {
        return rentalRepository.findById(id);
    }

    public Rental createRental(Rental rental) {
        Customer customer = customerRepository.findById(rental.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para locação"));
        rental.setCustomer(customer);

        InventoryItem inventoryItem = inventoryItemRepository.findByProductCode(rental.getProductCode())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado para locação"));
        if (inventoryItem.getQuantity() < rental.getQuantity()) {
            throw new RuntimeException("Estoque insuficiente para locação");
        }
        inventoryItem.removeStock(rental.getQuantity());
        inventoryItemRepository.save(inventoryItem);

        if (rental.getRentalCode() == null || rental.getRentalCode().isBlank()) {
            rental.setRentalCode("ALOC-" + System.currentTimeMillis());
        }

        if (rental.getExpectedReturnDate() == null && rental.getStartDate() != null) {
            rental.setExpectedReturnDate(rental.getStartDate().plusDays(rental.getRentalDays()));
        }

        rental.calculateTotal();
        return rentalRepository.save(rental);
    }

    public Rental returnRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        if ("RETURNED".equals(rental.getStatus())) {
            throw new RuntimeException("Locação já devolvida");
        }

        InventoryItem inventoryItem = inventoryItemRepository.findByProductCode(rental.getProductCode())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado para devolução"));
        inventoryItem.addStock(rental.getQuantity());
        inventoryItemRepository.save(inventoryItem);

        rental.setActualReturnDate(LocalDate.now());
        rental.setStatus("RETURNED");
        rental.setUpdatedAt(java.time.LocalDateTime.now());
        return rentalRepository.save(rental);
    }
}
