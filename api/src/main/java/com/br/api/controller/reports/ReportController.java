package com.br.api.controller.reports;

import com.br.api.model.reports.SalesReport;
import com.br.api.service.reports.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final SalesReportService salesReportService;

    public ReportController(SalesReportService salesReportService) {
        this.salesReportService = salesReportService;
    }

    @GetMapping("/sales/daily")
    public ResponseEntity<SalesReport> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            SalesReport report = salesReportService.generateDailyReport(date);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<SalesReport> getMonthlySalesReport(
            @RequestParam int year, 
            @RequestParam int month) {
        try {
            SalesReport report = salesReportService.generateMonthlyReport(year, month);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sales/range")
    public ResponseEntity<SalesReport> getSalesReportInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // Implementar relatório por período personalizado
        return ResponseEntity.ok(new SalesReport());
    }
}