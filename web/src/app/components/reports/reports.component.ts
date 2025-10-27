import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
    totalSales: 156,
    totalRevenue: 125430.50,
    averageSale: 804.04,
    itemsSold: 423
  };

  inventoryData = {
    totalItems: 1234,
    totalValue: 287650.75,
    lowStock: 15,
    outOfStock: 8
  };

  topProducts = [
    { name: 'Notebook Dell Inspiron', category: 'Eletrônicos', sold: 45, revenue: 157495.50 },
    { name: 'Mouse Logitech MX', category: 'Periféricos', sold: 89, revenue: 26611.10 },
    { name: 'Monitor 24" Samsung', category: 'Eletrônicos', sold: 32, revenue: 28799.68 },
    { name: 'Teclado Mecânico', category: 'Periféricos', sold: 28, revenue: 12600.00 },
    { name: 'Headphone Sony', category: 'Áudio', sold: 25, revenue: 8750.00 }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }
}