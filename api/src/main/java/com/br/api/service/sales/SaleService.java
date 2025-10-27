package com.br.api.service.sales;

import com.br.api.model.sales.Sale;
import com.br.api.model.sales.SaleItem;
import com.br.api.repository.sales.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    public Optional<Sale> findBySaleCode(String saleCode) {
        return saleRepository.findBySaleCode(saleCode);
    }

    public List<Sale> findByStatus(String status) {
        return saleRepository.findByStatus(status);
    }

    public List<Sale> findSalesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    @Transactional
    public Sale createSale(Sale sale) {
        // Gerar código da venda
        String saleCode = "SALE-" + System.currentTimeMillis();
        sale.setSaleCode(saleCode);
        
        // Calcular total
        sale.calculateTotal();
        
        // Associar itens à venda
        if (sale.getItems() != null) {
            for (SaleItem item : sale.getItems()) {
                item.setSale(sale);
            }
        }
        
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale updateSale(Long id, Sale saleDetails) {
        return saleRepository.findById(id)
            .map(sale -> {
                sale.setStatus(saleDetails.getStatus());
                sale.setNotes(saleDetails.getNotes());
                
                // Atualizar itens se fornecidos
                if (saleDetails.getItems() != null) {
                    sale.getItems().clear();
                    sale.getItems().addAll(saleDetails.getItems());
                    for (SaleItem item : sale.getItems()) {
                        item.setSale(sale);
                    }
                    sale.calculateTotal();
                }
                
                return saleRepository.save(sale);
            })
            .orElseThrow(() -> new RuntimeException("Venda não encontrada com id: " + id));
    }

    @Transactional
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    public Object[] getSalesSummary(LocalDateTime start, LocalDateTime end) {
        return saleRepository.getSalesSummary(start, end);
    }
}