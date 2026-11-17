import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InventoryService } from '../../services/inventory.service';
import { ReportService } from '../../services/report.service';
import { SaleService } from '../../services/sale.service';
import { Company, CompanyService } from '../../services/company.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  currentUser: any = null;
  company: Company | null = null;

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
    private saleService: SaleService,
    private companyService: CompanyService
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.companyService.load().subscribe({ next: company => this.company = company });
    this.loadReports();
  }

  printReport(): void {
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) return;
    printWindow.document.write(`
      <html><head><title>Relatório</title></head>
      <body style="font-family:Arial,sans-serif;padding:24px;color:#111;">
        ${this.company?.logoData ? `<img src="${this.company.logoData}" alt="Logo" style="max-width:180px;max-height:80px;">` : ''}
        <h1>${this.company?.name || 'Empresa'}</h1>
        <p>${[this.company?.document, this.company?.phone, this.company?.email].filter(Boolean).join(' | ')}</p>
        <p>${[this.company?.address, this.company?.addressNumber, this.company?.neighborhood, this.company?.city, this.company?.state, this.company?.zipCode].filter(Boolean).join(', ')}</p>
        <hr>
        <h2>Relatório de vendas e estoque</h2>
        <h3>Vendas</h3>
        <p>Vendas totais: ${this.salesData.totalSales} | Receita: ${this.salesData.totalRevenue.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} | Ticket médio: ${this.salesData.averageSale.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} | Itens: ${this.salesData.itemsSold}</p>
        <h3>Estoque</h3>
        <p>Itens: ${this.inventoryData.totalItems} | Valor: ${this.inventoryData.totalValue.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} | Estoque baixo: ${this.inventoryData.lowStock} | Sem estoque: ${this.inventoryData.outOfStock}</p>
        <h3>Produtos mais vendidos</h3>
        <ul>${this.topProducts.map(product => `<li>${product.name}: ${product.sold} vendas, ${product.revenue.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</li>`).join('')}</ul>
        <hr><p>${this.company?.printFooter || ''}</p>
      </body></html>`);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => printWindow.print(), 300);
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
          const productKey = String(item.inventoryItemId ?? item.productCode ?? item.productName);
          const current = products.get(productKey) || {
            name: item.productName,
            category: item.category || 'Produtos',
            sold: 0,
            revenue: 0
          };
          current.sold += item.quantity;
          current.revenue += item.quantity * item.unitPrice;
          products.set(productKey, current);
        }));
        this.topProducts = [...products.values()].sort((a, b) => b.sold - a.sold).slice(0, 5);
      },
      error: error => console.error('Erro ao carregar produtos vendidos:', error)
    });
  }
}
