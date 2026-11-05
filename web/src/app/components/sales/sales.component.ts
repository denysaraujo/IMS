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
  sales: Sale[] = [];
  customers: Customer[] = [];
  inventoryProducts: InventoryItem[] = [];
  selectedInventoryItemId: number | null = null;
  barcodeLookup = '';
  selectedCustomer: Customer | null = null;
  isCreatingSale: boolean = false;
  isProcessingSale = false;
  readonly cancelingSaleIds = new Set<number>();

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
    private inventoryService: InventoryService,
    private companyService: CompanyService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadSales();
    this.loadCustomers();
    this.loadInventoryProducts();
    this.companyService.load().subscribe({
      next: company => this.company = company,
      error: error => console.error('Erro ao carregar os dados da empresa para o comprovante:', error)
    });
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

  loadInventoryProducts(): void {
    this.inventoryService.getItems().subscribe({
      next: products => this.inventoryProducts = products,
      error: error => {
        console.error('Erro ao carregar produtos do estoque para a venda:', error);
        alert('Não foi possível carregar os produtos do estoque.');
      }
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

  selectInventoryProduct(inventoryItemId: number | null): void {
    this.selectedInventoryItemId = inventoryItemId;
    const product = this.inventoryProducts.find(item => item.id === inventoryItemId);
    if (!product) {
      this.newItem = { productCode: '', productName: '', quantity: 1, unitPrice: 0 };
      return;
    }

    this.newItem = {
      inventoryItemId: product.id,
      productCode: product.productCode,
      productName: product.productName,
      category: product.category,
      description: product.description,
      quantity: 1,
      unitPrice: product.unitPrice
    };
  }

  get selectedInventoryProduct(): InventoryItem | undefined {
    return this.inventoryProducts.find(item => item.id === this.selectedInventoryItemId);
  }

  applyBarcodeToSaleItem(barcode: string): void {
    this.barcodeLookup = barcode?.trim() ?? '';
    if (!this.barcodeLookup) {
      return;
    }

    const product = this.inventoryProducts.find(item => item.productCode === this.barcodeLookup);
    if (!product) {
      alert('Produto não encontrado no estoque.');
      return;
    }

    this.selectInventoryProduct(product.id ?? null);
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

  addItem(): void {
    const product = this.inventoryProducts.find(item => item.id === this.selectedInventoryItemId);
    if (!product) {
      alert('Selecione um produto cadastrado no estoque.');
      return;
    }
    const quantityAlreadyInSale = this.newSale.items
      .filter(item => item.inventoryItemId === product.id)
      .reduce((total, item) => total + item.quantity, 0);
    if (!Number.isInteger(this.newItem.quantity) || this.newItem.quantity <= 0
        || quantityAlreadyInSale + this.newItem.quantity > product.quantity
        || !Number.isFinite(this.newItem.unitPrice) || this.newItem.unitPrice < 0) {
      alert('Confira a quantidade disponível e o valor unitário informado.');
      return;
    }

    if (product.id != null) {
      this.newSale.items.push({...this.newItem});
      this.calculateTotal();
      this.selectInventoryProduct(null);
      this.barcodeLookup = '';
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
    if (this.isProcessingSale) {
      return;
    }
    if (this.selectedCustomer?.id == null) {
      alert('Selecione um cliente para a venda');
      return;
    }

    if (this.newSale.items.length === 0) {
      alert('Adicione pelo menos um item à venda');
      return;
    }

    this.newSale.customer = this.selectedCustomer;
    this.isProcessingSale = true;
    this.saleService.createSale(this.newSale).subscribe({
      next: (sale) => {
        this.isProcessingSale = false;
        alert('Venda processada com sucesso!');
        this.sales = [sale, ...this.sales];
        this.isCreatingSale = false;
        this.selectedCustomer = null;
        this.loadInventoryProducts();
      },
      error: (error) => {
        this.isProcessingSale = false;
        console.error('Erro ao processar venda:', error);
        alert('Erro ao processar venda: ' + (error.error?.message || error.message));
      }
    });
  }

  cancelSale(sale: Sale): void {
    if (sale.id == null || sale.status !== 'COMPLETED' || this.cancelingSaleIds.has(sale.id)) {
      return;
    }
    if (!window.confirm(`Cancelar a venda "${sale.saleCode}" e devolver os produtos ao estoque?`)) {
      return;
    }

    const saleId = sale.id;
    this.cancelingSaleIds.add(saleId);
    this.saleService.cancelSale(saleId).subscribe({
      next: () => {
        this.cancelingSaleIds.delete(saleId);
        this.sales = this.sales.map(entry =>
          entry.id === saleId ? { ...entry, status: 'CANCELLED' } : entry
        );
        this.loadInventoryProducts();
        alert('Venda cancelada e produtos devolvidos ao estoque.');
      },
      error: error => {
        this.cancelingSaleIds.delete(saleId);
        console.error('Erro ao cancelar venda:', error);
        alert('Não foi possível cancelar a venda. Ela pode já ter sido cancelada.');
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

  private formatCurrency(value: number): string {
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  private formatThermalLine(left: string, right?: string, width = 42): string {
    const leftText = left.trim();
    const rightText = right?.trim() ?? '';

    if (!rightText) {
      return leftText.padEnd(width, ' ').slice(0, width);
    }

    return `${leftText.padEnd(Math.max(10, width - rightText.length), ' ')}${rightText}`.slice(0, width);
  }

  generateReceipt(sale: Sale): string {
    const itemsText = sale.items?.map(item => {
      const productName = `${item.productName || 'Produto'}${item.category ? ` (${item.category})` : ''}`;
      const itemTotal = this.formatCurrency((item.quantity ?? 0) * (item.unitPrice ?? 0));
      const lines = [
        productName,
        item.description ? `  ${item.description}` : '',
        this.formatThermalLine(`${(item.quantity ?? 0)}x ${this.formatCurrency(item.unitPrice ?? 0)}`, itemTotal)
      ].filter(Boolean);

      return lines.join('\n');
    }).join('\n') || 'Nenhum item';

    const address = [this.company?.address, this.company?.city, this.company?.state].filter(Boolean).join(', ');
    const footer = this.company?.printFooter || 'Obrigado pela preferência!';
    const saleTotal = this.formatCurrency(sale.totalAmount || 0);

    return [
      (this.company?.name || 'Empresa').toUpperCase(),
      '================================',
      'RECIBO DE VENDA',
      '================================',
      `CNPJ: ${this.company?.document || 'Não informado'}`,
      `Endereço: ${address || 'Não informado'}`,
      `Telefone: ${this.company?.phone || 'Não informado'}`,
      `E-mail: ${this.company?.email || 'Não informado'}`,
      '',
      `Código: ${sale.saleCode}`,
      `Cliente: ${sale.customer?.name || 'Consumidor'}`,
      `Data: ${new Date(sale.saleDate).toLocaleString('pt-BR')}`,
      '--------------------------------',
      itemsText,
      '--------------------------------',
      this.formatThermalLine('TOTAL', saleTotal),
      `Total vendido: ${saleTotal}`,
      '================================',
      footer.toUpperCase()
    ].join('\n');
  }

  printReceipt(sale: Sale): void {
    const receipt = this.generateReceipt(sale);
    const printWindow = window.open('', '_blank', 'width=400,height=700');
    if (printWindow) {
      const escapeHtml = (value: string) => value.replace(/[&<>"']/g, character => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
      })[character] ?? character);

      printWindow.document.write(`
        <html>
          <head>
            <title>Recibo de venda</title>
            <style>
              @page { size: 80mm auto; margin: 0; }
              html, body {
                margin: 0;
                padding: 8px;
                background: #fff;
                color: #111827;
                width: 80mm;
                box-sizing: border-box;
              }
              body {
                font-family: "Courier New", Courier, monospace;
                font-size: 11px;
                line-height: 1.35;
                letter-spacing: 0.2px;
              }
              pre {
                margin: 0;
                white-space: pre-wrap;
                word-break: break-word;
                font-family: inherit;
                font-size: inherit;
                line-height: inherit;
              }
              .center { text-align: center; }
            </style>
          </head>
          <body>
            ${this.company?.logoData ? `<img src="${escapeHtml(this.company.logoData)}" alt="Logo" style="display:block;max-width:120px;max-height:60px;margin:0 auto 8px;">` : ''}
            <pre>${escapeHtml(receipt)}</pre>
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
