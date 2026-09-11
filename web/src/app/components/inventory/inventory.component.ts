import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Inventory, InventoryItem, InventoryService } from '../../services/inventory.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  currentUser: any = null;
  inventoryItems: any[] = [];
  categories: string[] = [];
  inventories: Inventory[] = [];
  newProduct = {
    barcode: '',
    name: '',
    category: 'Eletrônicos',
    quantity: 1,
    unitPrice: 0,
    minStock: 5,
    maxStock: 20
  };

  readonly barcodeCatalog: Record<string, { name: string; category: string; unitPrice: number; minStock: number; maxStock: number }> = {
    '7891234560011': { name: 'Notebook Dell Inspiron', category: 'Eletrônicos', unitPrice: 3499.99, minStock: 5, maxStock: 50 },
    '7891234560028': { name: 'Mouse Logitech MX', category: 'Periféricos', unitPrice: 299.90, minStock: 10, maxStock: 100 },
    '7891234560035': { name: 'Teclado Mecânico', category: 'Periféricos', unitPrice: 450.00, minStock: 5, maxStock: 50 },
    '7891234560042': { name: 'Monitor 24" Samsung', category: 'Eletrônicos', unitPrice: 899.99, minStock: 3, maxStock: 20 },
    '7891234560059': { name: 'Cadeira Gamer', category: 'Móveis', unitPrice: 1200.00, minStock: 2, maxStock: 10 },
    '7891234560066': { name: 'Headphone Sony', category: 'Áudio', unitPrice: 350.00, minStock: 5, maxStock: 30 }
  };

  constructor(
    private authService: AuthService,
    private router: Router,
    private inventoryService: InventoryService
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
    this.inventoryService.getInventories().subscribe({
      next: inventories => this.inventories = inventories,
      error: error => console.error('Erro ao carregar inventários:', error)
    });

    this.inventoryService.getItems().subscribe({
      next: items => {
        this.inventoryItems = items.map(item => this.toViewItem(item));
        this.categories = [...new Set(this.inventoryItems.map(item => item.category))];
      },
      error: error => console.error('Erro ao carregar itens do estoque:', error)
    });
  }

  private toViewItem(item: InventoryItem): any {
    return {
      ...item,
      code: item.productCode,
      name: item.productName,
      minStock: item.minStockLevel,
      maxStock: item.maxStockLevel,
      barcode: item.productCode
    };
  }

  applyBarcodeToInventoryProduct(barcode: string) {
    const normalized = barcode?.trim();
    if (!normalized) {
      return;
    }

    this.newProduct.barcode = normalized;
    const item = this.barcodeCatalog[normalized];
    if (item) {
      this.newProduct.name = item.name;
      this.newProduct.category = item.category;
      this.newProduct.unitPrice = item.unitPrice;
      this.newProduct.minStock = item.minStock;
      this.newProduct.maxStock = item.maxStock;
    }
  }

  async readInventoryBarcode() {
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
        this.applyBarcodeToInventoryProduct(result[0].rawValue);
      } else {
        alert('Nenhum código de barras foi detectado. Tente novamente.');
      }

      stream.getTracks().forEach(track => track.stop());
    } catch (error) {
      console.error('Erro ao ler código de barras do estoque:', error);
      alert('Não foi possível acessar a câmera. Use o campo manualmente.');
    }
  }

  addInventoryProduct() {
    if (!this.newProduct.barcode || !this.newProduct.name || this.newProduct.quantity <= 0) {
      alert('Informe o código de barras e o nome do produto para cadastrar.');
      return;
    }

    const inventoryId = this.inventories[0]?.id;
    if (!inventoryId) {
      alert('Nenhum inventário disponível para receber o produto.');
      return;
    }

    const item: InventoryItem = {
      productCode: this.newProduct.barcode,
      productName: this.newProduct.name,
      category: this.newProduct.category,
      quantity: this.newProduct.quantity,
      minStockLevel: this.newProduct.minStock,
      maxStockLevel: this.newProduct.maxStock,
      unitPrice: this.newProduct.unitPrice,
      expirationDate: '2099-12-31'
    };

    this.inventoryService.createItem(inventoryId, item).subscribe({
      next: created => {
        this.inventoryItems.unshift(this.toViewItem(created));
        this.categories = [...new Set(this.inventoryItems.map(entry => entry.category))];
        this.resetNewProduct();
        alert('Produto cadastrado com sucesso no estoque.');
      },
      error: error => {
        console.error('Erro ao cadastrar produto:', error);
        alert('Não foi possível cadastrar o produto no estoque.');
      }
    });
  }

  private resetNewProduct(): void {
    this.newProduct = {
      barcode: '',
      name: '',
      category: 'Eletrônicos',
      quantity: 1,
      unitPrice: 0,
      minStock: 5,
      maxStock: 20
    };
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
