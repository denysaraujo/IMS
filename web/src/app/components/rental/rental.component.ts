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
  selectedCustomer: any = null;
  isCreatingRental = false;

  rentalForm = {
    rentalCode: '',
    customerName: '',
    productCode: '',
    productName: '',
    quantity: 1,
    dailyRate: 0,
    rentalDays: 1,
    startDate: new Date().toISOString().slice(0, 10),
    notes: ''
  };

  readonly barcodeCatalog: Record<string, { productName: string; dailyRate: number }> = {
    '7891234561011': { productName: 'Câmera Canon', dailyRate: 120 },
    '7891234561028': { productName: 'Projetor Epson', dailyRate: 200 },
    '7891234561035': { productName: 'Luz de Estúdio', dailyRate: 90 },
    '7891234561042': { productName: 'Tripé profissional', dailyRate: 60 }
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
    this.rentalService.getRentals().subscribe({
      next: rentals => this.rentals = rentals.map(rental => ({
        ...rental,
        customerName: rental.customer?.name || 'Cliente'
      })),
      error: error => console.error('Erro ao carregar locações:', error)
    });
  }

  loadCustomers(): void {
    this.customerService.getCustomers().subscribe({
      next: customers => this.customers = customers,
      error: error => console.error('Erro ao carregar clientes:', error)
    });
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
      productCode: '',
      productName: '',
      quantity: 1,
      dailyRate: 0,
      rentalDays: 1,
      startDate: new Date().toISOString().slice(0, 10),
      notes: ''
    };
  }

  applyBarcodeToRental(barcode: string): void {
    const normalized = barcode?.trim();
    if (!normalized) {
      return;
    }

    this.rentalForm.productCode = normalized;
    const item = this.barcodeCatalog[normalized];
    if (item) {
      this.rentalForm.productName = item.productName;
      this.rentalForm.dailyRate = item.dailyRate;
      return;
    }

    this.inventoryService.getItemByCode(normalized).subscribe({
      next: (inventoryItem: InventoryItem) => {
        this.rentalForm.productName = inventoryItem.productName;
        this.rentalForm.dailyRate = inventoryItem.unitPrice;
      },
      error: () => console.warn('Produto não encontrado para o código:', normalized)
    });
  }

  async readRentalBarcode(): Promise<void> {
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
        this.applyBarcodeToRental(result[0].rawValue);
      } else {
        alert('Nenhum código de barras foi detectado. Tente novamente.');
      }

      stream.getTracks().forEach(track => track.stop());
    } catch (error) {
      console.error('Erro ao ler código de barras da locação:', error);
      alert('Não foi possível acessar a câmera. Use o campo manualmente.');
    }
  }

  saveRental(): void {
    if (!this.rentalForm.customerName || !this.rentalForm.productCode || !this.rentalForm.productName) {
      alert('Informe cliente, produto e código para registrar a locação.');
      return;
    }

    const customer = this.customers.find(item => item.name.toLowerCase() === this.rentalForm.customerName.trim().toLowerCase());
    if (!customer?.id) {
      alert('Selecione um cliente cadastrado na API informando o nome exato.');
      return;
    }

    const rental: Rental = {
      rentalCode: this.rentalForm.rentalCode,
      customer,
      productCode: this.rentalForm.productCode,
      productName: this.rentalForm.productName,
      quantity: Number(this.rentalForm.quantity),
      dailyRate: Number(this.rentalForm.dailyRate),
      rentalDays: Number(this.rentalForm.rentalDays),
      startDate: this.rentalForm.startDate,
      totalAmount: Number(this.rentalForm.dailyRate) * Number(this.rentalForm.quantity) * Number(this.rentalForm.rentalDays),
      depositAmount: 0,
      status: 'ACTIVE',
      notes: this.rentalForm.notes
    };

    this.rentalService.createRental(rental).subscribe({
      next: created => {
        this.rentals.unshift({ ...created, customerName: created.customer?.name || customer.name });
        this.isCreatingRental = false;
        this.stripForm();
        alert('Locação cadastrada com sucesso!');
      },
      error: error => {
        console.error('Erro ao cadastrar locação:', error);
        alert('Não foi possível cadastrar a locação.');
      }
    });
  }

  returnRental(item: any): void {
    if (!item.id) {
      return;
    }

    this.rentalService.returnRental(item.id).subscribe({
      next: returned => {
        Object.assign(item, returned, { customerName: returned.customer?.name || item.customerName });
        alert(`Devolução registrada para ${item.rentalCode}.`);
      },
      error: error => {
        console.error('Erro ao registrar devolução:', error);
        alert('Não foi possível registrar a devolução.');
      }
    });
  }

  generateReceipt(rental: any): string {
    return [
      this.company?.name || 'Empresa',
      [this.company?.document, this.company?.phone].filter(Boolean).join(' | '),
      [this.company?.address, this.company?.city, this.company?.state].filter(Boolean).join(', '),
      '',
      'RECIBO DE LOCAÇÃO',
      '================',
      `Código: ${rental.rentalCode}`,
      `Cliente: ${rental.customerName}`,
      `Produto: ${rental.productName}`,
      `Quantidade: ${rental.quantity}`,
      `Valor diário: R$ ${Number(rental.dailyRate || 0).toFixed(2)}`,
      `Período: ${rental.rentalDays} dias`,
      `Total: R$ ${Number(rental.totalAmount || 0).toFixed(2)}`,
      '================',
      this.company?.printFooter || 'Obrigado pela preferência!'
    ].join('\n');
  }

  printReceipt(rental: any): void {
    const receipt = this.generateReceipt(rental);
    const printWindow = window.open('', '_blank', 'width=500,height=700');
    if (printWindow) {
      printWindow.document.write(`
        <html>
          <head><title>Recibo de locação</title></head>
          <body style="font-family: Arial, sans-serif; padding: 20px;">
            ${this.company?.logoData ? `<img src="${this.company.logoData}" alt="Logo" style="max-width:180px;max-height:80px;display:block;margin:0 auto 16px;">` : ''}
            <h2 style="text-align:center;margin:0;">${this.company?.name || 'Empresa'}</h2>
            <pre>${receipt}</pre>
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.focus();
      setTimeout(() => printWindow.print(), 300);
    }
  }
}
