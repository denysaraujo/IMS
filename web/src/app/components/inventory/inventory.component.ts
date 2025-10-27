import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  currentUser: any = null;
  inventoryItems: any[] = [];
  categories: string[] = [];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadInventory();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadInventory() {
    // Mock data - substituir por chamada API
    this.inventoryItems = [
      {
        id: 1,
        code: 'PROD001',
        name: 'Notebook Dell Inspiron',
        category: 'Eletrônicos',
        quantity: 15,
        unitPrice: 3499.99,
        minStock: 5,
        maxStock: 50
      },
      {
        id: 2,
        code: 'PROD002',
        name: 'Mouse Logitech MX',
        category: 'Periféricos',
        quantity: 42,
        unitPrice: 299.90,
        minStock: 10,
        maxStock: 100
      },
      {
        id: 3,
        code: 'PROD003',
        name: 'Teclado Mecânico',
        category: 'Periféricos',
        quantity: 23,
        unitPrice: 450.00,
        minStock: 5,
        maxStock: 50
      },
      {
        id: 4,
        code: 'PROD004',
        name: 'Monitor 24" Samsung',
        category: 'Eletrônicos',
        quantity: 8,
        unitPrice: 899.99,
        minStock: 3,
        maxStock: 20
      },
      {
        id: 5,
        code: 'PROD005',
        name: 'Cadeira Gamer',
        category: 'Móveis',
        quantity: 3,
        unitPrice: 1200.00,
        minStock: 2,
        maxStock: 10
      },
      {
        id: 6,
        code: 'PROD006',
        name: 'Headphone Sony',
        category: 'Áudio',
        quantity: 0,
        unitPrice: 350.00,
        minStock: 5,
        maxStock: 30
      }
    ];

    this.categories = [...new Set(this.inventoryItems.map(item => item.category))];
  }

  getTotalItems(): number {
    return this.inventoryItems.reduce((sum, item) => sum + item.quantity, 0);
  }

  getTotalValue(): number {
    return this.inventoryItems.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  }

  getLowStockCount(): number {
    return this.inventoryItems.filter(item => item.quantity > 0 && item.quantity <= item.minStock).length;
  }

  getOutOfStockCount(): number {
    return this.inventoryItems.filter(item => item.quantity === 0).length;
  }

  getStockStatusClass(item: any): string {
    if (item.quantity === 0) {
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    } else if (item.quantity <= item.minStock) {
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    } else if (item.quantity >= item.maxStock) {
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
    } else {
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    }
  }

  getStockStatusText(item: any): string {
    if (item.quantity === 0) {
      return 'Sem Estoque';
    } else if (item.quantity <= item.minStock) {
      return 'Estoque Baixo';
    } else if (item.quantity >= item.maxStock) {
      return 'Estoque Alto';
    } else {
      return 'Em Estoque';
    }
  }
}