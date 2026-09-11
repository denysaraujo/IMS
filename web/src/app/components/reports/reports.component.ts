import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InventoryService } from '../../services/inventory.service';
import { ReportService } from '../../services/report.service';
import { SaleService } from '../../services/sale.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  currentUser: any = null;

  salesData = {
    totalSales: 0,
    totalRevenue: 0,
    averageSale: 0,
    itemsSold: 0
  };

  inventoryData = {
    totalItems: 0,
    totalValue: 0,
    lowStock: 0,
    outOfStock: 0
  };

  topProducts: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private reportService: ReportService,
    private inventoryService: InventoryService,
    private saleService: SaleService
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadReports();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadReports(): void {
    const today = new Date();
    const date = today.toISOString().slice(0, 10);

    this.reportService.getDailySalesReport(date).subscribe({
      next: report => {
        this.salesData = {
          totalSales: report.totalSales || 0,
          totalRevenue: report.totalRevenue || 0,
          averageSale: report.averageSaleValue || 0,
          itemsSold: report.totalItemsSold || 0
        };
      },
      error: error => console.error('Erro ao carregar relatório de vendas:', error)
    });

    this.inventoryService.getItems().subscribe({
      next: items => {
        this.inventoryData = {
          totalItems: items.reduce((total, item) => total + (item.quantity || 0), 0),
          totalValue: items.reduce((total, item) => total + (item.quantity || 0) * (item.unitPrice || 0), 0),
          lowStock: items.filter(item => item.quantity > 0 && item.quantity <= item.minStockLevel).length,
          outOfStock: items.filter(item => item.quantity <= 0).length
        };
      },
      error: error => console.error('Erro ao carregar relatório de estoque:', error)
    });

    this.saleService.getSales().subscribe({
      next: sales => {
        const products = new Map<string, { name: string; category: string; sold: number; revenue: number }>();
        sales.forEach(sale => sale.items?.forEach(item => {
          const current = products.get(item.productCode) || {
            name: item.productName,
            category: 'Produtos',
            sold: 0,
            revenue: 0
          };
          current.sold += item.quantity;
          current.revenue += item.quantity * item.unitPrice;
          products.set(item.productCode, current);
        }));
        this.topProducts = [...products.values()].sort((a, b) => b.sold - a.sold).slice(0, 5);
      },
      error: error => console.error('Erro ao carregar produtos vendidos:', error)
    });
  }
}
