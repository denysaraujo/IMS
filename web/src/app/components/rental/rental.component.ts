import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Customer, CustomerService } from '../../services/customer.service';
import { Rental, RentalService } from '../../services/rental.service';
import { InventoryItem, InventoryService } from '../../services/inventory.service';
import { Company, CompanyService } from '../../services/company.service';

@Component({
  selector: 'app-rental',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rental.component.html',
  styleUrls: ['./rental.component.css']
})
export class RentalComponent implements OnInit {
  currentUser: any = null;
  company: Company | null = null;
  rentals: any[] = [];
  customers: Customer[] = [];
  inventoryItems: InventoryItem[] = [];
  selectedCustomer: any = null;
  isCreatingRental = false;
  isLoadingRentals = false;
  rentalLoadError = '';
  isSavingRental = false;
  returningRentalIds = new Set<number>();
  deletingRentalIds = new Set<number>();
  barcodeLookup = '';
  receiptPreviewTitle = '';
  receiptPreview = '';

  rentalForm = {
    rentalCode: '',
    customerName: '',
    inventoryItemId: null as number | null,
    quantity: 1,
    dailyRate: 0,
    rentalDays: 1,
    startDate: new Date().toISOString().slice(0, 10),
    notes: ''
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private customerService: CustomerService,
    private rentalService: RentalService,
    private inventoryService: InventoryService,
    private companyService: CompanyService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadRentals();
    this.loadCustomers();
    this.loadInventoryItems();
    this.companyService.load().subscribe({ next: company => this.company = company });
    this.generateCode();
  }

  loadUserData(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  generateCode(): void {
    this.rentalForm.rentalCode = `ALOC-${Date.now()}`;
  }

  loadRentals(): void {
    this.isLoadingRentals = true;
    this.rentalLoadError = '';
    this.rentalService.getRentals().subscribe({
      next: rentals => {
        this.rentals = rentals.map(rental => ({
          ...rental,
          customerName: rental.customer?.name || 'Cliente'
        }));
        this.isLoadingRentals = false;
      },
      error: error => {
        console.error('Erro ao carregar locações:', error);
        this.rentalLoadError = 'Não foi possível carregar o histórico de locações.';
        this.isLoadingRentals = false;
      }
    });
  }

  loadCustomers(): void {
    this.customerService.getCustomers().subscribe({
      next: customers => this.customers = customers,
      error: error => console.error('Erro ao carregar clientes:', error)
    });
  }

  loadInventoryItems(): void {
    this.inventoryService.getItems().subscribe({
      next: items => this.inventoryItems = items,
      error: error => {
        console.error('Erro ao carregar produtos do inventário para locação:', error);
        alert('Não foi possível carregar os produtos do inventário.');
      }
    });
  }

  get availableInventoryItems(): InventoryItem[] {
    return this.inventoryItems.filter(item => item.availableForRental === true && item.quantity > 0);
  }

  getAvailableQuantity(inventoryItemId: number): number {
    return this.inventoryItems.find(item => item.id === inventoryItemId)?.quantity ?? 0;
  }

  startNewRental(): void {
    this.isCreatingRental = true;
    this.selectedCustomer = null;
    this.generateCode();
  }

  cancelRental(): void {
    this.isCreatingRental = false;
    this.stripForm();
  }

  stripForm(): void {
    this.rentalForm = {
      rentalCode: '',
      customerName: '',
      inventoryItemId: null,
      quantity: 1,
      dailyRate: 0,
      rentalDays: 1,
      startDate: new Date().toISOString().slice(0, 10),
      notes: ''
    };
  }

  selectInventoryItem(inventoryItemId: number | null): void {
    this.rentalForm.inventoryItemId = inventoryItemId;
    const item = this.inventoryItems.find(inventoryItem => inventoryItem.id === inventoryItemId);
    this.rentalForm.dailyRate = item?.unitPrice ?? 0;
  }

  applyBarcodeToRental(barcode: string): void {
    const normalized = barcode?.trim();
    if (!normalized) {
      return;
    }

    const item = this.inventoryItems.find(inventoryItem =>
      inventoryItem.productCode === normalized && inventoryItem.availableForRental === true
    );
    if (!item || item.quantity <= 0) {
      alert('Produto não encontrado no inventário ou sem estoque disponível.');
      return;
    }

    this.selectInventoryItem(item.id ?? null);
    this.barcodeLookup = normalized;
  }

  async readRentalBarcode(): Promise<void> {
    const BarcodeDetectorCtor = (window as any).BarcodeDetector;
    if (!BarcodeDetectorCtor || !navigator.mediaDevices?.getUserMedia) {
      alert('Leitura por código de barras não está disponível neste navegador. Digite o código manualmente.');
      return;
    }

    let stream: MediaStream | undefined;
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      const video = document.createElement('video');
      video.srcObject = stream;
      video.setAttribute('playsinline', 'true');
      await video.play();

      const detector = new BarcodeDetectorCtor({ formats: ['code_128', 'ean_13', 'ean_8', 'qr_code'] });
      const result = await detector.detect(video);

      if (result && result.length > 0) {
        this.applyBarcodeToRental(result[0].rawValue);
      } else {
        alert('Nenhum código de barras foi detectado. Tente novamente.');
      }
    } catch (error) {
      console.error('Erro ao ler código de barras da locação:', error);
      alert('Não foi possível acessar a câmera. Use o campo manualmente.');
    } finally {
      stream?.getTracks().forEach(track => track.stop());
    }
  }

  saveRental(): void {
    if (this.isSavingRental) {
      return;
    }
    const inventoryItem = this.inventoryItems.find(item => item.id === this.rentalForm.inventoryItemId);
    if (!this.rentalForm.customerName || !inventoryItem) {
      alert('Selecione um cliente e um produto cadastrado no inventário.');
      return;
    }

    const customer = this.customers.find(item => item.name.toLowerCase() === this.rentalForm.customerName.trim().toLowerCase());
    if (!customer?.id) {
      alert('Selecione um cliente cadastrado.');
      return;
    }
    if (!Number.isInteger(this.rentalForm.quantity) || this.rentalForm.quantity <= 0
        || this.rentalForm.quantity > inventoryItem.quantity) {
      alert(`Informe uma quantidade válida. Disponível: ${inventoryItem.quantity}.`);
      return;
    }
    if (!Number.isInteger(this.rentalForm.rentalDays) || this.rentalForm.rentalDays <= 0
        || !Number.isFinite(this.rentalForm.dailyRate) || this.rentalForm.dailyRate < 0) {
      alert('Confira a duração e o valor diário da locação.');
      return;
    }

    const rental: Rental = {
      rentalCode: this.rentalForm.rentalCode,
      customer,
      inventoryItemId: inventoryItem.id,
      productCode: inventoryItem.productCode ?? '',
      productName: inventoryItem.productName,
      quantity: Number(this.rentalForm.quantity),
      dailyRate: Number(this.rentalForm.dailyRate),
      rentalDays: Number(this.rentalForm.rentalDays),
      startDate: this.rentalForm.startDate,
      totalAmount: Number(this.rentalForm.dailyRate) * Number(this.rentalForm.quantity) * Number(this.rentalForm.rentalDays),
      depositAmount: 0,
      status: 'ACTIVE',
      notes: this.rentalForm.notes
    };

    this.isSavingRental = true;
    this.rentalService.createRental(rental).subscribe({
      next: created => {
        this.rentals.unshift({ ...created, customerName: created.customer?.name || customer.name });
        this.isCreatingRental = false;
        this.stripForm();
        this.loadInventoryItems();
        this.isSavingRental = false;
        alert('Locação cadastrada com sucesso!');
      },
      error: error => {
        console.error('Erro ao cadastrar locação:', error);
        this.isSavingRental = false;
        this.loadInventoryItems();
        alert(error.error?.message || 'Não foi possível cadastrar a locação.');
      }
    });
  }

  returnRental(item: any): void {
    if (!item.id || this.returningRentalIds.has(item.id) || item.status === 'RETURNED') {
      return;
    }

    if (!window.confirm(`Confirmar a devolução da locação "${item.rentalCode}"? O produto retornará ao estoque.`)) {
      return;
    }

    this.returningRentalIds = new Set(this.returningRentalIds).add(item.id);
    this.rentalService.returnRental(item.id).subscribe({
      next: returned => {
        Object.assign(item, returned, { customerName: returned.customer?.name || item.customerName });
        this.returningRentalIds.delete(item.id);
        this.loadInventoryItems();
        alert(`Devolução registrada para ${item.rentalCode}.`);
      },
      error: error => {
        console.error('Erro ao registrar devolução:', error);
        this.returningRentalIds.delete(item.id);
        alert(error.error?.message || 'Não foi possível registrar a devolução.');
      }
    });
  }

  deleteRental(item: any): void {
    if (!item.id || this.deletingRentalIds.has(item.id) || item.status === 'RETURNED') {
      return;
    }

    if (!window.confirm(`Excluir a locação "${item.rentalCode}"? Esta ação removerá o registro e devolverá o estoque ao inventário.`)) {
      return;
    }

    this.deletingRentalIds = new Set(this.deletingRentalIds).add(item.id);
    this.rentalService.deleteRental(item.id).subscribe({
      next: () => {
        this.rentals = this.rentals.filter(rental => rental.id !== item.id);
        this.deletingRentalIds.delete(item.id);
        this.loadInventoryItems();
        alert(`Locação "${item.rentalCode}" excluída com sucesso.`);
      },
      error: error => {
        console.error('Erro ao excluir locação:', error);
        this.deletingRentalIds.delete(item.id);
        alert(error.error?.message || 'Não foi possível excluir a locação.');
      }
    });
  }

  generateReceipt(rental: any): string {
    const startDate = rental.startDate ? new Date(rental.startDate).toLocaleDateString('pt-BR') : 'Não informado';
    const endDate = rental.actualReturnDate
      ? new Date(rental.actualReturnDate).toLocaleDateString('pt-BR')
      : 'Em aberto';

    return [
      this.company?.name || 'Empresa',
      [this.company?.document, this.company?.phone].filter(Boolean).join(' | '),
      [this.company?.address, this.company?.addressNumber, this.company?.neighborhood, this.company?.city, this.company?.state, this.company?.zipCode].filter(Boolean).join(', '),
      '',
      'RECIBO DE LOCAÇÃO',
      '================',
      `Código: ${rental.rentalCode}`,
      `Cliente: ${rental.customerName}`,
      `Produto: ${rental.productName}`,
      `Quantidade: ${rental.quantity}`,
      `Valor diário: ${Number(rental.dailyRate || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}`,
      `Período: ${rental.rentalDays} dias`,
      `Data de início: ${startDate}`,
      `Data de devolução: ${endDate}`,
      `Total: ${Number(rental.totalAmount || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}`,
      '================',
      this.company?.printFooter || 'Obrigado pela preferência!'
    ].join('\n');
  }

  generateReturnReceipt(rental: any): string {
    const returnedAt = rental.actualReturnDate
      ? new Date(rental.actualReturnDate).toLocaleDateString('pt-BR')
      : new Date().toLocaleDateString('pt-BR');

    return [
      this.company?.name || 'Empresa',
      [this.company?.document, this.company?.phone].filter(Boolean).join(' | '),
      [this.company?.address, this.company?.addressNumber, this.company?.neighborhood, this.company?.city, this.company?.state, this.company?.zipCode].filter(Boolean).join(', '),
      '',
      'RECIBO DE DEVOLUÇÃO DE LOCAÇÃO',
      '================',
      `Código: ${rental.rentalCode}`,
      `Cliente: ${rental.customerName}`,
      `Produto: ${rental.productName}`,
      `Quantidade: ${rental.quantity}`,
      `Data da devolução: ${returnedAt}`,
      `Valor total locado: ${Number(rental.totalAmount || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}`,
      `Status: ${rental.status === 'RETURNED' ? 'Devolvido' : 'Concluído'}`,
      '================',
      this.company?.printFooter || 'Obrigado pela preferência!'
    ].join('\n');
  }

  previewReceipt(rental: Rental, isReturnReceipt = false): void {
    this.receiptPreviewTitle = isReturnReceipt ? 'Recibo de devolução de locação' : 'Recibo de locação';
    this.receiptPreview = isReturnReceipt
      ? this.generateReturnReceipt(rental)
      : this.generateReceipt(rental);
  }

  closeReceiptPreview(): void {
    this.receiptPreview = '';
    this.receiptPreviewTitle = '';
  }

  printReceiptPreview(): void {
    if (this.receiptPreview) {
      this.openReceiptWindow(this.receiptPreviewTitle, this.receiptPreview);
    }
  }

  private openReceiptWindow(title: string, receipt: string): void {
    const printWindow = window.open('', '_blank', 'width=500,height=700');
    if (!printWindow) {
      alert('O navegador bloqueou a janela de impressão. Permita pop-ups para este site e tente novamente.');
      return;
    }

    const document = printWindow.document;
    document.title = title;
    const style = document.createElement('style');
    style.textContent = 'body { font-family: Arial, sans-serif; padding: 24px; color: #111827; } pre { white-space: pre-wrap; word-break: break-word; font-family: inherit; } .logo { display: block; max-width: 180px; max-height: 80px; margin: 0 auto 16px; } .company-name { text-align: center; margin: 0 0 12px; font-size: 20px; }';
    document.head.appendChild(style);
    if (this.company?.logoData) {
      const logo = document.createElement('img');
      logo.src = this.company.logoData;
      logo.alt = 'Logo';
      logo.className = 'logo';
      document.body.appendChild(logo);
    }
    const heading = document.createElement('h2');
    heading.className = 'company-name';
    heading.textContent = this.company?.name || 'Empresa';
    document.body.appendChild(heading);
    const content = document.createElement('pre');
    content.textContent = receipt;
    document.body.appendChild(content);
    printWindow.focus();
    setTimeout(() => printWindow.print(), 300);
  }

  printReceipt(rental: Rental): void {
    this.openReceiptWindow('Recibo de locação', this.generateReceipt(rental));
  }

  printReturnReceipt(rental: Rental): void {
    this.openReceiptWindow('Recibo de devolução de locação', this.generateReturnReceipt(rental));
  }
}
