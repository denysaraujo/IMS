// SaleService.java
package com.br.api.service.sales;

import com.br.api.model.customer.Customer;
import com.br.api.model.sales.Sale;
import com.br.api.model.sales.SaleItem;
import com.br.api.model.inventory.InventoryItem;
import com.br.api.repository.sales.SaleRepository;
import com.br.api.repository.inventory.InventoryItemRepository;
import com.br.api.repository.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SaleService {
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public Sale processSale(Sale sale) {
        // Validar se o cliente existe
        Customer customer = customerRepository.findById(sale.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + sale.getCustomer().getId()));
        
        sale.setCustomer(customer);

        // Validar estoque antes de processar
        for (SaleItem item : sale.getItems()) {
            Optional<InventoryItem> inventoryItemOpt = inventoryItemRepository.findByProductCode(item.getProductCode());
            
            if (inventoryItemOpt.isEmpty()) {
                throw new RuntimeException("Produto não encontrado: " + item.getProductCode());
            }
            
            InventoryItem inventoryItem = inventoryItemOpt.get();
            
            if (inventoryItem.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para: " + inventoryItem.getProductName() + 
                                         ". Disponível: " + inventoryItem.getQuantity() + 
                                         ", Solicitado: " + item.getQuantity());
            }
            
            item.setInventoryItem(inventoryItem);
        }
        
        sale.processSale();
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
        
        sale.cancelSale();
        saleRepository.save(sale);
    }
}