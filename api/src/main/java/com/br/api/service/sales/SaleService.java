// SaleService.java
package com.br.api.service.sales;

import com.br.api.model.customer.Customer;
import com.br.api.model.inventory.Inventory;
import com.br.api.model.sales.Sale;
import com.br.api.model.sales.SaleItem;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.sales.SaleRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.inventory.InventoryRepository;
import com.br.api.repository.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    public Sale processSale(Sale sale) {
        if (sale.getCustomer() == null || sale.getCustomer().getId() == null) {
            throw new RuntimeException("Cliente é obrigatório para processar a venda");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new RuntimeException("A venda deve conter pelo menos um produto");
        }

        // Validar se o cliente existe
        Customer customer = customerRepository.findById(sale.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + sale.getCustomer().getId()));
        
        sale.setCustomer(customer);

        Map<Long, Integer> requestedQuantities = new LinkedHashMap<>();
        Map<Long, InventoryItem> inventoryItems = new LinkedHashMap<>();
        for (SaleItem item : sale.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() <= 0
                    || item.getUnitPrice() == null || !Double.isFinite(item.getUnitPrice())
                    || item.getUnitPrice() < 0) {
                throw new RuntimeException("Quantidade ou valor inválido para o produto da venda");
            }

            Optional<InventoryItem> inventoryItemOpt = item.getInventoryItemId() != null
                ? inventoryItemRepository.findById(item.getInventoryItemId())
                : item.getProductCode() == null
                    ? Optional.empty()
                    : inventoryItemRepository.findByProductCode(item.getProductCode());
            
            if (inventoryItemOpt.isEmpty()) {
                throw new RuntimeException("Produto de estoque não encontrado");
            }
            
            InventoryItem inventoryItem = inventoryItemOpt.get();
            requestedQuantities.merge(inventoryItem.getId(), item.getQuantity(), Integer::sum);
            inventoryItems.put(inventoryItem.getId(), inventoryItem);
            item.setInventoryItem(inventoryItem);
            item.setSale(sale);
            item.setProductCode(inventoryItem.getProductCode());
            item.setProductName(inventoryItem.getProductName());
            item.setCategory(inventoryItem.getCategory());
            item.setDescription(inventoryItem.getDescription());
        }

        for (Map.Entry<Long, Integer> request : requestedQuantities.entrySet()) {
            InventoryItem inventoryItem = inventoryItems.get(request.getKey());
            if (inventoryItem.getQuantity() < request.getValue()) {
                throw new RuntimeException("Estoque insuficiente para: " + inventoryItem.getProductName()
                    + ". Disponível: " + inventoryItem.getQuantity()
                    + ", Solicitado: " + request.getValue());
            }
        }
        
        sale.processSale();

        Map<Long, Inventory> affectedInventories = new LinkedHashMap<>();
        for (InventoryItem inventoryItem : inventoryItems.values()) {
            inventoryItemRepository.save(inventoryItem);
            if (inventoryItem.getInventory() != null) {
                affectedInventories.put(inventoryItem.getInventory().getId(), inventoryItem.getInventory());
            }
        }
        for (Inventory inventory : affectedInventories.values()) {
            inventory.calculateTotals();
            inventoryRepository.save(inventory);
        }

        return saleRepository.save(sale);
    }
    
    public List<Sale> getSalesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate);
    }
    
    public List<Sale> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId);
    }
    
    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }
    
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }
    
    public void cancelSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        if (!"COMPLETED".equals(sale.getStatus())) {
            throw new RuntimeException("Somente vendas concluídas podem ser canceladas");
        }

        sale.setStatus("CANCELLED");
        for (SaleItem item : sale.getItems()) {
            InventoryItem inventoryItem = item.getInventoryItem();
            if (inventoryItem == null && item.getProductCode() != null) {
                inventoryItem = inventoryItemRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado para estorno: " + item.getProductCode()));
                item.setInventoryItem(inventoryItem);
            }
            if (inventoryItem == null) {
                throw new RuntimeException("Produto não encontrado para estorno: " + item.getProductName());
            }
            inventoryItem.addStock(item.getQuantity());
            inventoryItemRepository.save(inventoryItem);
            if (inventoryItem.getInventory() != null) {
                inventoryItem.getInventory().calculateTotals();
                inventoryRepository.save(inventoryItem.getInventory());
            }
        }
        saleRepository.save(sale);
    }

    public void deleteSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        if ("CANCELLED".equals(sale.getStatus())) {
            throw new RuntimeException("Venda cancelada não pode ser excluída");
        }

        for (SaleItem item : sale.getItems()) {
            InventoryItem inventoryItem = item.getInventoryItem();
            if (inventoryItem == null && item.getProductCode() != null) {
                inventoryItem = inventoryItemRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado para exclusão: " + item.getProductCode()));
                item.setInventoryItem(inventoryItem);
            }
            if (inventoryItem == null) {
                throw new RuntimeException("Produto não encontrado para exclusão: " + item.getProductName());
            }
            if ("COMPLETED".equals(sale.getStatus())) {
                inventoryItem.addStock(item.getQuantity());
                inventoryItemRepository.save(inventoryItem);
                if (inventoryItem.getInventory() != null) {
                    inventoryItem.getInventory().calculateTotals();
                    inventoryRepository.save(inventoryItem.getInventory());
                }
            }
        }

        saleRepository.delete(sale);
    }
}