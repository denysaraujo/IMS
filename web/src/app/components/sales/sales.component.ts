import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { SaleService, Sale, SaleItem } from '../../services/sale.service';
import { CustomerService, Customer } from '../../services/customer.service';
import { InventoryItem, InventoryService } from '../../services/inventory.service';
import { Company, CompanyService } from '../../services/company.service';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sales.component.html',
  styleUrls: ['./sales.component.css']
})
export class SalesComponent implements OnInit {
  currentUser: any = null;
  company: Company | null = null;
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

  readonly barcodeCatalog: Record<string, { productName: string; unitPrice: number }> = {
    '7891234560011': { productName: 'Notebook Dell Inspiron', unitPrice: 3499.99 },
    '7891234560028': { productName: 'Mouse Logitech MX', unitPrice: 299.90 },
    '7891234560035': { productName: 'Teclado Mecânico', unitPrice: 450.00 },
    '7891234560042': { productName: 'Monitor 24" Samsung', unitPrice: 899.99 },
    '7891234560059': { productName: 'Cadeira Gamer', unitPrice: 1200.00 },
    '7891234560066': { productName: 'Headphone Sony', unitPrice: 350.00 }
  };

  constructor(
    private authService: AuthService,
    private saleService: SaleService,
    private customerService: CustomerService,
    private inventoryService: InventoryService,
    private companyService: CompanyService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadSales();
    this.loadCustomers();
    this.companyService.load().subscribe({ next: company => this.company = company });
    this.generateSaleCode();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadSales() {
    this.saleService.getSales().subscribe({
      next: sales => this.sales = sales,
      error: error => console.error('Erro ao carregar vendas:', error)
    });
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

  applyBarcodeToSaleItem(barcode: string) {
    const normalized = barcode?.trim();
    if (!normalized) {
      return;
    }

    this.newItem.productCode = normalized;
    const item = this.barcodeCatalog[normalized];
    if (item) {
      this.newItem.productName = item.productName;
      this.newItem.unitPrice = item.unitPrice;
      return;
    }

    this.inventoryService.getItemByCode(normalized).subscribe({
      next: (inventoryItem: InventoryItem) => {
        this.newItem.productName = inventoryItem.productName;
        this.newItem.unitPrice = inventoryItem.unitPrice;
      },
      error: () => console.warn('Produto não encontrado para o código:', normalized)
    });
  }

  async readBarcodeFromDevice() {
    const BarcodeDetectorCtor = (window as any).BarcodeDetector;
    if (!BarcodeDetectorCtor || !navigator.mediaDevices?.getUserMedia) {
      alert('Leitura por código de barras não está disponível neste navegador. Digite o código manualmente.');
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      const video = document.createElement('video');
      video.srcObject = stream;
      video.setAttribute('playsinline', 'true');
      await video.play();

      const detector = new BarcodeDetectorCtor({ formats: ['code_128', 'ean_13', 'ean_8', 'qr_code'] });
      const result = await detector.detect(video);

      if (result && result.length > 0) {
        const code = result[0].rawValue;
        this.applyBarcodeToSaleItem(code);
      } else {
        alert('Nenhum código de barras foi detectado. Tente novamente.');
      }

      stream.getTracks().forEach(track => track.stop());
    } catch (error) {
      console.error('Erro ao ler código de barras:', error);
      alert('Não foi possível acessar a câmera. Use o campo manualmente.');
    }
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
        this.sales = [sale, ...this.sales];
        this.isCreatingSale = false;
        this.selectedCustomer = null;
      },
      error: (error) => {
        console.error('Erro ao processar venda:', error);
        alert('Erro ao processar venda: ' + (error.error?.message || error.message));
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

  generateReceipt(sale: any): string {
    const itemsText = sale.items?.map((item: any) =>
      `- ${item.name || item.productName}: ${item.quantity} x R$ ${Number(item.price || item.unitPrice || 0).toFixed(2)}`
    ).join('\n') || 'Nenhum item';

    return [
      this.company?.name || 'Empresa',
      [this.company?.document, this.company?.phone].filter(Boolean).join(' | '),
      [this.company?.address, this.company?.city, this.company?.state].filter(Boolean).join(', '),
      '',
      'RECIBO DE VENDA',
      '================',
      `Código: ${sale.saleCode}`,
      `Cliente: ${sale.customer?.name || 'Consumidor'}`,
      `Data: ${new Date(sale.saleDate).toLocaleString('pt-BR')}`,
      '',
      itemsText,
      '',
      `Total: R$ ${Number(sale.totalAmount || 0).toFixed(2)}`,
      '================',
      this.company?.printFooter || 'Obrigado pela preferência!'
    ].join('\n');
  }

  printReceipt(sale: any): void {
    const receipt = this.generateReceipt(sale);
    const printWindow = window.open('', '_blank', 'width=500,height=700');
    if (printWindow) {
      printWindow.document.write(`
        <html>
          <head>
            <title>Recibo de venda</title>
            <style>
              body { font-family: Arial, sans-serif; padding: 24px; color: #111827; }
              pre { white-space: pre-wrap; word-break: break-word; font-family: inherit; }
              .logo { display: block; max-width: 180px; max-height: 80px; margin: 0 auto 16px; }
              .company-name { text-align: center; margin: 0 0 12px; font-size: 20px; }
            </style>
          </head>
          <body>
            ${this.company?.logoData ? `<img src="${this.company.logoData}" alt="Logo" class="logo">` : ''}
            <h2 class="company-name">${this.company?.name || 'Empresa'}</h2>
            <pre>${receipt}</pre>
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.focus();
      setTimeout(() => printWindow.print(), 300);
    }
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
