import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { SaleService, Sale, SaleItem } from '../../services/sale.service';
import { CustomerService, Customer } from '../../services/customer.service';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sales.component.html',
  styleUrls: ['./sales.component.css']
})
export class SalesComponent implements OnInit {
  currentUser: any = null;
  sales: any[] = []; // Usei any[] temporariamente para evitar erros
  customers: Customer[] = [];
  selectedCustomer: Customer | null = null;
  isCreatingSale: boolean = false;
  
  // newSale inicializado corretamente
  newSale: Sale = {
    saleCode: '',
    saleDate: new Date().toISOString(),
    totalAmount: 0,
    status: 'PENDING',
    customer: {} as Customer,
    items: []
  };

  // Produtos para a nova venda
  newItem: SaleItem = {
    productCode: '',
    productName: '',
    quantity: 1,
    unitPrice: 0
  };

  constructor(
    private authService: AuthService,
    private saleService: SaleService,
    private customerService: CustomerService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadSales();
    this.loadCustomers();
    this.generateSaleCode();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadSales() {
    // Mock data temporário para evitar erros de tipo
    this.sales = [
      {
        id: 1001,
        saleCode: 'V20240115001', 
        customer: {
          name: 'João Silva',
          email: 'joao@email.com' 
        },
        saleDate: '2024-01-15 14:30:00', 
        items: [
          { name: 'Notebook Dell', quantity: 1, price: 3499.99 },
          { name: 'Mouse Logitech', quantity: 1, price: 299.90 }
        ],
        totalAmount: 3799.89, 
        status: 'COMPLETED'
      },
      {
        id: 1002,
        saleCode: 'V20240115002',
        customer: {
          name: 'Maria Santos', 
          email: 'maria@email.com'
        },
        saleDate: '2024-01-15 16:45:00',
        items: [
          { name: 'Monitor 24"', quantity: 2, price: 899.99 }
        ],
        totalAmount: 1799.98,
        status: 'COMPLETED'
      }
    ];

    // Descomente quando a API estiver pronta:
    // this.saleService.getSales().subscribe({
    //   next: (sales) => {
    //     this.sales = sales;
    //   },
    //   error: (error) => console.error('Erro ao carregar vendas:', error)
    // });
  }

  loadCustomers() {
    this.customerService.getCustomers().subscribe({
      next: (customers) => {
        this.customers = customers;
      },
      error: (error) => console.error('Erro ao carregar clientes:', error)
    });
  }

  generateSaleCode() {
    const timestamp = new Date().getTime();
    this.newSale.saleCode = `V${timestamp}`;
  }

  startNewSale() {
    this.isCreatingSale = true;
    this.newSale = {
      saleCode: '',
      saleDate: new Date().toISOString(),
      totalAmount: 0,
      status: 'PENDING',
      customer: {} as Customer,
      items: []
    };
    this.generateSaleCode();
    this.selectedCustomer = null;
  }

  addItem() {
    if (this.newItem.productCode && this.newItem.productName && this.newItem.quantity > 0 && this.newItem.unitPrice > 0) {
      this.newSale.items.push({...this.newItem});
      this.calculateTotal();
      this.newItem = {
        productCode: '',
        productName: '',
        quantity: 1,
        unitPrice: 0
      };
    }
  }

  removeItem(index: number) {
    this.newSale.items.splice(index, 1);
    this.calculateTotal();
  }

  calculateTotal() {
    this.newSale.totalAmount = this.newSale.items.reduce((total, item) => 
      total + (item.quantity * item.unitPrice), 0
    );
  }

  selectCustomerForSale(customer: Customer) {
    this.selectedCustomer = customer;
    this.newSale.customer = customer;
  }

  processSale() {
    if (!this.selectedCustomer) {
      alert('Selecione um cliente para a venda');
      return;
    }

    if (this.newSale.items.length === 0) {
      alert('Adicione pelo menos um item à venda');
      return;
    }

    this.saleService.createSale(this.newSale).subscribe({
      next: (sale) => {
        alert('Venda processada com sucesso!');
        this.loadSales();
        this.isCreatingSale = false;
        this.selectedCustomer = null;
      },
      error: (error) => {
        console.error('Erro ao processar venda:', error);
        alert('Erro ao processar venda: ' + error.error?.message || error.message);
      }
    });
  }

  cancelSaleCreation() {
    this.isCreatingSale = false;
    this.selectedCustomer = null;
    this.newSale.items = [];
  }

  getTotalSales(): number {
    return this.sales.length;
  }

  getTotalRevenue(): number {
    return this.sales.reduce((sum, sale) => sum + sale.totalAmount, 0);
  }

  getTodaySales(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.sales.filter(sale => sale.saleDate.startsWith(today)).length;
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