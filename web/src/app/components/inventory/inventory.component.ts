import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, switchMap, tap, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { CreateInventoryRequest, Inventory, InventoryItem, InventoryService } from '../../services/inventory.service';
import { InventoryCreateModalComponent } from './inventory-create-modal/inventory-create-modal.component';

type InventoryItemView = InventoryItem & {
  code: string | null;
  name: string;
  minStock: number;
  maxStock: number;
  barcode: string | null;
};

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule, InventoryCreateModalComponent],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  currentUser: any = null;
  inventoryItems: InventoryItemView[] = [];
  categories: string[] = [];
  inventories: Inventory[] = [];
  selectedInventoryId: number | null = null;
  isCreateInventoryModalOpen = false;
  inventoryBeingEdited: Inventory | null = null;
  isSavingInventory = false;
  isLoadingInventories = false;
  isSavingProduct = false;
  editingProductId: number | null = null;
  editingBarcodeItemId: number | null = null;
  editedBarcode = '';
  newProduct = {
    barcode: '',
    name: '',
    category: 'Eletrônicos',
    description: '',
    quantity: 1,
    unitPrice: 0,
    minStock: 5,
    maxStock: 20,
    supplier: '',
    location: '',
    expirationDate: '2099-12-31'
  };
  searchTerm = '';
  selectedCategoryFilter = '';

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

  focusNewProductForm(input: HTMLInputElement): void {
    input.scrollIntoView({ behavior: 'smooth', block: 'center' });
    input.focus();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();

    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadInventory() {
    this.isLoadingInventories = true;
    this.inventoryService.getInventories().subscribe({
      next: inventories => {
        this.inventories = inventories;
        this.selectedInventoryId = inventories.find(inventory => inventory.id != null)?.id ?? null;
        this.isLoadingInventories = false;
        this.refreshInventoryTotals();
      },
      error: error => {
        this.isLoadingInventories = false;
        console.error('Erro ao carregar inventários:', error);
        alert('Não foi possível carregar os inventários.');
      }
    });

    this.inventoryService.getItems().subscribe({
      next: items => {
        this.inventoryItems = items.map(item => this.toViewItem(item));
        this.refreshCategories();
        this.refreshInventoryTotals();
      },
      error: error => {
        console.error('Erro ao carregar itens do estoque:', error);
        alert('Não foi possível carregar os produtos do estoque.');
      }
    });
  }

  openCreateInventoryModal(): void {
    this.inventoryBeingEdited = null;
    this.isCreateInventoryModalOpen = true;
  }

  editInventory(inventory: Inventory): void {
    this.inventoryBeingEdited = inventory;
    this.isCreateInventoryModalOpen = true;
  }

  closeCreateInventoryModal(): void {
    if (!this.isSavingInventory) {
      this.isCreateInventoryModalOpen = false;
      this.inventoryBeingEdited = null;
    }
  }

  createInventory(inventory: CreateInventoryRequest): void {
    if (this.isSavingInventory) {
      return;
    }

    this.isSavingInventory = true;
    const inventoryBeingEdited = this.inventoryBeingEdited;
    const save$ = inventoryBeingEdited?.id != null
      ? this.inventoryService.updateInventory(inventoryBeingEdited.id, inventory)
      : this.inventoryService.createInventory(inventory);

    save$.subscribe({
      next: savedInventory => {
        this.inventories = inventoryBeingEdited
          ? this.inventories.map(entry => entry.id === savedInventory.id ? savedInventory : entry)
          : [savedInventory, ...this.inventories];
        if (!inventoryBeingEdited || this.selectedInventoryId == null) {
          this.selectedInventoryId = savedInventory.id ?? null;
        }
        this.isSavingInventory = false;
        this.isCreateInventoryModalOpen = false;
        this.inventoryBeingEdited = null;
        alert(inventoryBeingEdited ? 'Inventário atualizado com sucesso.' : 'Inventário criado com sucesso.');
      },
      error: error => {
        this.isSavingInventory = false;
        console.error('Erro ao salvar inventário:', error);
        alert('Não foi possível salvar o inventário. Verifique os dados e tente novamente.');
      }
    });
  }

  deleteInventory(inventory: Inventory): void {
    if (inventory.id == null || !window.confirm(`Excluir o inventário "${inventory.name}" e todos os produtos vinculados?`)) {
      return;
    }

    this.inventoryService.deleteInventory(inventory.id).subscribe({
      next: () => {
        this.inventories = this.inventories.filter(entry => entry.id !== inventory.id);
        const deletedProducts = this.inventoryItems.filter(item => item.inventoryId === inventory.id);
        this.inventoryItems = this.inventoryItems.filter(item => item.inventoryId !== inventory.id);
        if (deletedProducts.some(item => item.id === this.editingProductId)) {
          this.cancelProductEdit();
        }
        if (deletedProducts.some(item => item.id === this.editingBarcodeItemId)) {
          this.cancelBarcodeEdit();
        }
        if (this.selectedInventoryId === inventory.id) {
          this.selectedInventoryId = this.inventories.find(entry => entry.id != null)?.id ?? null;
        }
        this.refreshCategories();
        this.refreshInventoryTotals();
        alert('Inventário e produtos vinculados excluídos com sucesso.');
      },
      error: error => {
        console.error('Erro ao excluir inventário:', error);
        alert('Não foi possível excluir o inventário.');
      }
    });
  }

  private toViewItem(item: InventoryItem): InventoryItemView {
    return {
      ...item,
      inventoryId: item.inventoryId ?? item.inventory?.id,
      code: item.productCode,
      name: item.productName,
      minStock: item.minStockLevel,
      maxStock: item.maxStockLevel,
      barcode: item.productCode
    };
  }

  applyBarcodeToInventoryProduct(barcode: string) {
    const normalized = barcode?.trim() ?? '';
    this.newProduct.barcode = normalized;
    if (!normalized) {
      return;
    }

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
    if (this.isSavingProduct || this.isLoadingInventories) {
      return;
    }

    if (!this.newProduct.name.trim() || !this.newProduct.category.trim()
      || !Number.isInteger(this.newProduct.quantity) || this.newProduct.quantity < 0
      || !Number.isFinite(this.newProduct.unitPrice) || this.newProduct.unitPrice < 0
      || !Number.isInteger(this.newProduct.minStock) || this.newProduct.minStock < 0
      || !Number.isInteger(this.newProduct.maxStock) || this.newProduct.maxStock < this.newProduct.minStock
      || !this.newProduct.expirationDate) {
      alert('Preencha os campos obrigatórios e confira quantidade, preço e limites de estoque.');
      return;
    }

    if (this.editingProductId != null) {
      this.updateInventoryProduct();
      return;
    }

    const selectedInventory = this.inventories.find(inventory => inventory.id === this.selectedInventoryId);
    const inventory$ = selectedInventory
      ? of(selectedInventory)
      : this.inventoryService.createInventory({
          name: 'Estoque principal',
          location: 'Loja',
          category: 'Geral'
        }).pipe(
          tap(inventory => {
            this.inventories = [inventory, ...this.inventories];
            this.selectedInventoryId = inventory.id ?? null;
          })
        );

    this.isSavingProduct = true;
    inventory$.pipe(
      switchMap(inventory => {
        if (inventory.id == null) {
          return throwError(() => new Error('A API criou um inventário sem retornar seu identificador.'));
        }
        return this.inventoryService.createItem(inventory.id, this.buildProductPayload(inventory.id));
      })
    ).subscribe({
      next: created => {
        this.isSavingProduct = false;
        this.inventoryItems.unshift(this.toViewItem(created));
        this.refreshCategories();
        this.refreshInventoryTotals();
        this.resetNewProduct();
        alert('Produto cadastrado com sucesso no estoque.');
      },
      error: error => {
        this.isSavingProduct = false;
        console.error('Erro ao cadastrar produto:', error);
        alert('Não foi possível cadastrar o produto no estoque.');
      }
    });
  }

  editInventoryProduct(item: InventoryItemView): void {
    if (item.id == null) {
      return;
    }
    this.editingProductId = item.id;
    this.selectedInventoryId = item.inventoryId ?? this.selectedInventoryId;
    this.newProduct = {
      barcode: item.productCode ?? '',
      name: item.productName,
      category: item.category,
      description: item.description ?? '',
      quantity: item.quantity,
      unitPrice: item.unitPrice,
      minStock: item.minStockLevel,
      maxStock: item.maxStockLevel,
      supplier: item.supplier ?? '',
      location: item.location ?? '',
      expirationDate: item.expirationDate ?? '2099-12-31'
    };
  }

  cancelProductEdit(): void {
    this.editingProductId = null;
    this.resetNewProduct();
  }

  private updateInventoryProduct(): void {
    if (this.editingProductId == null || this.selectedInventoryId == null) {
      alert('Selecione um inventário para o produto.');
      return;
    }

    this.isSavingProduct = true;
    this.inventoryService.updateItem(
      this.editingProductId,
      this.buildProductPayload(this.selectedInventoryId)
    ).subscribe({
      next: savedItem => {
        const index = this.inventoryItems.findIndex(item => item.id === savedItem.id);
        if (index >= 0) {
          this.inventoryItems[index] = this.toViewItem(savedItem);
        }
        this.isSavingProduct = false;
        this.cancelProductEdit();
        this.refreshCategories();
        this.refreshInventoryTotals();
        alert('Produto atualizado com sucesso.');
      },
      error: error => {
        this.isSavingProduct = false;
        console.error('Erro ao atualizar produto:', error);
        alert('Não foi possível atualizar o produto. Verifique os dados e tente novamente.');
      }
    });
  }

  deleteInventoryProduct(item: InventoryItemView): void {
    if (item.id == null || !window.confirm(`Excluir o produto "${item.productName}"?`)) {
      return;
    }

    this.inventoryService.deleteItem(item.id).subscribe({
      next: () => {
        this.inventoryItems = this.inventoryItems.filter(entry => entry.id !== item.id);
        if (this.editingProductId === item.id) {
          this.cancelProductEdit();
        }
        if (this.editingBarcodeItemId === item.id) {
          this.cancelBarcodeEdit();
        }
        this.refreshCategories();
        this.refreshInventoryTotals();
        alert('Produto excluído com sucesso.');
      },
      error: error => {
        console.error('Erro ao excluir produto:', error);
        alert('Não foi possível excluir o produto.');
      }
    });
  }

  private buildProductPayload(inventoryId: number): InventoryItem {
    return {
      productCode: this.newProduct.barcode.trim() || null,
      productName: this.newProduct.name.trim(),
      description: this.newProduct.description.trim() || undefined,
      category: this.newProduct.category.trim(),
      quantity: this.newProduct.quantity,
      minStockLevel: this.newProduct.minStock,
      maxStockLevel: this.newProduct.maxStock,
      unitPrice: this.newProduct.unitPrice,
      supplier: this.newProduct.supplier.trim() || undefined,
      location: this.newProduct.location.trim() || undefined,
      expirationDate: this.newProduct.expirationDate,
      inventoryId
    };
  }

  editBarcode(item: InventoryItemView): void {
    if (item.id == null) {
      return;
    }

    this.editingBarcodeItemId = item.id;
    this.editedBarcode = item.productCode ?? '';
  }

  cancelBarcodeEdit(): void {
    this.editingBarcodeItemId = null;
    this.editedBarcode = '';
  }

  saveBarcode(item: InventoryItemView): void {
    if (item.id == null) {
      return;
    }

    const updatedItem: InventoryItem = {
      id: item.id,
      productCode: this.editedBarcode.trim() || null,
      productName: item.productName,
      description: item.description,
      category: item.category,
      quantity: item.quantity,
      minStockLevel: item.minStockLevel,
      maxStockLevel: item.maxStockLevel,
      unitPrice: item.unitPrice,
      supplier: item.supplier,
      location: item.location,
      status: item.status,
      expirationDate: item.expirationDate,
      inventoryId: item.inventoryId,
      inventory: item.inventory
    };

    this.inventoryService.updateItem(item.id, updatedItem).subscribe({
      next: savedItem => {
        const index = this.inventoryItems.findIndex(entry => entry.id === savedItem.id);
        if (index !== -1) {
          this.inventoryItems[index] = this.toViewItem(savedItem);
        }
        this.refreshInventoryTotals();
        this.cancelBarcodeEdit();
        alert('Código de barras atualizado com sucesso.');
      },
      error: error => {
        console.error('Erro ao atualizar código de barras:', error);
        alert('Não foi possível atualizar o código de barras.');
      }
    });
  }

  private resetNewProduct(): void {
    this.editingProductId = null;
    this.newProduct = {
      barcode: '',
      name: '',
      category: 'Eletrônicos',
      description: '',
      quantity: 1,
      unitPrice: 0,
      minStock: 5,
      maxStock: 20,
      supplier: '',
      location: '',
      expirationDate: '2099-12-31'
    };
  }

  private refreshCategories(): void {
    this.categories = [...new Set(this.inventoryItems.map(item => item.category))];
  }

  private refreshInventoryTotals(): void {
    this.inventories = this.inventories.map(inventory => {
      const items = this.inventoryItems.filter(item => item.inventoryId === inventory.id);
      return {
        ...inventory,
        totalItems: items.length,
        totalValue: items.reduce((total, item) => total + item.quantity * item.unitPrice, 0)
      };
    });
  }

  getInventoryName(inventoryId: number | undefined): string {
    return this.inventories.find(inventory => inventory.id === inventoryId)?.name ?? 'Inventário não identificado';
  }

  get filteredInventoryItems(): InventoryItemView[] {
    const search = this.searchTerm.trim().toLocaleLowerCase();
    return this.inventoryItems.filter(item => {
      const matchesSearch = !search
        || item.productName.toLocaleLowerCase().includes(search)
        || (item.productCode ?? '').toLocaleLowerCase().includes(search);
      const matchesCategory = !this.selectedCategoryFilter || item.category === this.selectedCategoryFilter;
      return matchesSearch && matchesCategory;
    });
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
