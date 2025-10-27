package com.br.api.controller.sales;

import com.br.api.model.sales.Sale;
import com.br.api.service.sales.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> getAllSales() {
        return saleService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        Optional<Sale> sale = saleService.findById(id);
        return sale.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{saleCode}")
    public ResponseEntity<Sale> getSaleByCode(@PathVariable String saleCode) {
        Optional<Sale> sale = saleService.findBySaleCode(saleCode);
        return sale.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Sale> getSalesByStatus(@PathVariable String status) {
        return saleService.findByStatus(status);
    }

    @PostMapping
    public Sale createSale(@RequestBody Sale sale) {
        return saleService.createSale(sale);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable Long id, @RequestBody Sale saleDetails) {
        try {
            Sale updatedSale = saleService.updateSale(id, saleDetails);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        try {
            saleService.deleteSale(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Object[]> getSalesSummary(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Object[] summary = saleService.getSalesSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}