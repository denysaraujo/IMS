package com.br.api.controller.sales;

import com.br.api.model.sales.Sale;
import com.br.api.repository.reports.SalesReportDTO;
import com.br.api.repository.sales.SaleRepository;
import com.br.api.service.sales.SaleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:4200")
public class SaleController {
    
    @Autowired
    private SaleService saleService;

    // CORREÇÃO: Injetar o SaleRepository corretamente
    @Autowired
    private SaleRepository saleRepository;
    
    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        try {
            Sale processedSale = saleService.processSale(sale);
            return ResponseEntity.ok(processedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/period")
    public ResponseEntity<List<Sale>> getSalesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Sale> sales = saleService.getSalesByPeriod(startDate, endDate);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/reports")
    public ResponseEntity<List<SalesReportDTO>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // CORREÇÃO: Usar o saleRepository injetado corretamente
        List<SalesReportDTO> report = saleRepository.findSalesReportByPeriod(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // CORREÇÃO: Adicionar endpoints faltantes
    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales() {
        List<Sale> sales = saleService.findAll();
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        Optional<Sale> sale = saleService.findById(id);
        return sale.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Sale>> getSalesByCustomer(@PathVariable Long customerId) {
        List<Sale> sales = saleService.getSalesByCustomer(customerId);
        return ResponseEntity.ok(sales);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(@PathVariable Long id) {
        try {
            saleService.cancelSale(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}