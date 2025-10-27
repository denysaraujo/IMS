import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sales.component.html',
  styleUrls: ['./sales.component.css']
})
export class SalesComponent implements OnInit {
  currentUser: any = null;
  sales: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadSales();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadSales() {
    // Mock data - substituir por chamada API
    this.sales = [
      {
        id: 1001,
        code: 'V20240115001',
        customer: 'João Silva',
        customerEmail: 'joao@email.com',
        date: '2024-01-15 14:30:00',
        items: [
          { name: 'Notebook Dell', quantity: 1, price: 3499.99 },
          { name: 'Mouse Logitech', quantity: 1, price: 299.90 }
        ],
        total: 3799.89,
        status: 'COMPLETED'
      },
      {
        id: 1002,
        code: 'V20240115002',
        customer: 'Maria Santos',
        customerEmail: 'maria@email.com',
        date: '2024-01-15 16:45:00',
        items: [
          { name: 'Monitor 24"', quantity: 2, price: 899.99 }
        ],
        total: 1799.98,
        status: 'COMPLETED'
      },
      {
        id: 1003,
        code: 'V20240116001',
        customer: 'Carlos Oliveira',
        customerEmail: 'carlos@email.com',
        date: '2024-01-16 09:15:00',
        items: [
          { name: 'Teclado Mecânico', quantity: 1, price: 450.00 },
          { name: 'Headphone Sony', quantity: 1, price: 350.00 }
        ],
        total: 800.00,
        status: 'PENDING'
      },
      {
        id: 1004,
        code: 'V20240116002',
        customer: 'Ana Costa',
        customerEmail: 'ana@email.com',
        date: '2024-01-16 11:20:00',
        items: [
          { name: 'Cadeira Gamer', quantity: 1, price: 1200.00 }
        ],
        total: 1200.00,
        status: 'CANCELLED'
      }
    ];
  }

  getTotalSales(): number {
    return this.sales.length;
  }

  getTotalRevenue(): number {
    return this.sales.reduce((sum, sale) => sum + sale.total, 0);
  }

  getTodaySales(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.sales.filter(sale => sale.date.startsWith(today)).length;
  }

  getMonthlyRevenue(): number {
    return this.getTotalRevenue() * 30; // Simulação
  }

  getPendingSales(): number {
    return this.sales.filter(sale => sale.status === 'PENDING').length;
  }

  getCancelledSales(): number {
    return this.sales.filter(sale => sale.status === 'CANCELLED').length;
  }

  getSaleStatusClass(status: string): string {
    switch(status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    }
  }

  getSaleStatusText(status: string): string {
    switch(status) {
      case 'COMPLETED': return 'Concluída';
      case 'PENDING': return 'Pendente';
      case 'CANCELLED': return 'Cancelada';
      default: return status;
    }
  }
}