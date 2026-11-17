package com.br.api.service.rental;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.Inventory;
import com.br.api.model.rental.Rental;
import com.br.api.repository.customer.CustomerRepository;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
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
    private final InventoryRepository inventoryRepository;

    public RentalService(RentalRepository rentalRepository, CustomerRepository customerRepository,
                         InventoryItemRepository inventoryItemRepository, InventoryRepository inventoryRepository) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Rental> findAll() {
        return rentalRepository.findAllWithCustomerOrderByCreatedAtDesc();
    }

    public Optional<Rental> findById(Long id) {
        return rentalRepository.findById(id);
    }

    public Rental createRental(Rental rental) {
        validateRental(rental);
        Customer customer = customerRepository.findById(rental.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para locação"));
        rental.setCustomer(customer);

        InventoryItem inventoryItem = findInventoryItemForUpdate(rental);
        if (!inventoryItem.isAvailableForRental()) {
            throw new RuntimeException("Este produto não está habilitado para locação");
        }
        if (inventoryItem.getQuantity() < rental.getQuantity()) {
            throw new RuntimeException("Estoque insuficiente para locação. Disponível: "
                + inventoryItem.getQuantity() + ", solicitado: " + rental.getQuantity());
        }
        inventoryItem.removeStock(rental.getQuantity());
        rental.setInventoryItem(inventoryItem);
        rental.setProductCode(inventoryItem.getProductCode());
        rental.setProductName(inventoryItem.getProductName());
        inventoryItemRepository.save(inventoryItem);
        updateInventoryTotals(inventoryItem);

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
        Rental rental = rentalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        if (!"ACTIVE".equals(rental.getStatus()) && !"OVERDUE".equals(rental.getStatus())) {
            throw new RuntimeException("Locação já devolvida");
        }

        InventoryItem inventoryItem = rental.getInventoryItem();
        if (inventoryItem == null && rental.getProductCode() != null) {
            inventoryItem = inventoryItemRepository.findByProductCodeForUpdate(rental.getProductCode())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado para devolução"));
        }
        if (inventoryItem == null) {
            throw new RuntimeException("Produto não encontrado para devolução");
        }
        inventoryItem.addStock(rental.getQuantity());
        inventoryItemRepository.save(inventoryItem);
        updateInventoryTotals(inventoryItem);

        rental.setActualReturnDate(LocalDate.now());
        rental.setStatus("RETURNED");
        rental.setUpdatedAt(java.time.LocalDateTime.now());
        rental.setCustomer(customerRepository.findById(rental.getCustomer().getId())
            .orElseThrow(() -> new RuntimeException("Cliente da locação não encontrado")));
        return rentalRepository.save(rental);
    }

    public void deleteRental(Long id) {
        Rental rental = rentalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Locação não encontrada"));

        if ("RETURNED".equals(rental.getStatus())) {
            throw new RuntimeException("Locação devolvida não pode ser excluída");
        }

        if (!"ACTIVE".equals(rental.getStatus()) && !"OVERDUE".equals(rental.getStatus())) {
            throw new RuntimeException("Status da locação não permite exclusão");
        }

        InventoryItem inventoryItem = rental.getInventoryItem();
        if (inventoryItem == null && rental.getProductCode() != null) {
            inventoryItem = inventoryItemRepository.findByProductCodeForUpdate(rental.getProductCode())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado para cancelar a locação"));
        }
        if (inventoryItem == null) {
            throw new RuntimeException("Produto não encontrado para cancelar a locação");
        }

        inventoryItem.addStock(rental.getQuantity());
        inventoryItemRepository.save(inventoryItem);
        updateInventoryTotals(inventoryItem);
        rentalRepository.delete(rental);
    }

    private void validateRental(Rental rental) {
        if (rental == null || rental.getCustomer() == null || rental.getCustomer().getId() == null) {
            throw new RuntimeException("Cliente é obrigatório para registrar a locação");
        }
        if (rental.getQuantity() == null || rental.getQuantity() <= 0
                || rental.getRentalDays() == null || rental.getRentalDays() <= 0) {
            throw new RuntimeException("Quantidade e duração da locação devem ser maiores que zero");
        }
        if (rental.getDailyRate() == null || !Double.isFinite(rental.getDailyRate()) || rental.getDailyRate() < 0
                || rental.getDepositAmount() != null
                && (!Double.isFinite(rental.getDepositAmount()) || rental.getDepositAmount() < 0)) {
            throw new RuntimeException("Valores da locação inválidos");
        }
        if (rental.getStartDate() == null) {
            throw new RuntimeException("Data de início é obrigatória para registrar a locação");
        }
        if (rental.getInventoryItemId() == null
                && (rental.getProductCode() == null || rental.getProductCode().isBlank())) {
            throw new RuntimeException("Selecione um produto cadastrado no inventário");
        }
    }

    private InventoryItem findInventoryItemForUpdate(Rental rental) {
        if (rental.getInventoryItemId() != null) {
            return inventoryItemRepository.findByIdForUpdate(rental.getInventoryItemId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado no inventário"));
        }
        return inventoryItemRepository.findByProductCodeForUpdate(rental.getProductCode())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado no inventário"));
    }

    private void updateInventoryTotals(InventoryItem inventoryItem) {
        Inventory inventory = inventoryItem.getInventory();
        if (inventory != null) {
            inventory.calculateTotals();
            inventoryRepository.save(inventory);
        }
    }
}
