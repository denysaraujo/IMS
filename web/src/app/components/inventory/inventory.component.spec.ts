import { of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CreateInventoryRequest, Inventory, InventoryItem, InventoryService } from '../../services/inventory.service';
import { InventoryComponent } from './inventory.component';

describe('InventoryComponent', () => {
  let component: InventoryComponent;
  let inventoryService: jasmine.SpyObj<InventoryService>;

  beforeEach(() => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['getCurrentUser']);
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    inventoryService = jasmine.createSpyObj<InventoryService>('InventoryService', [
      'getInventories',
      'getItems',
      'createInventory',
      'updateInventory',
      'deleteInventory',
      'createItem',
      'updateItem',
      'deleteItem'
    ]);
    component = new InventoryComponent(authService, router, inventoryService);
    spyOn(window, 'alert');
    spyOn(window, 'confirm').and.returnValue(true);
  });

  it('takes focus to the product form when starting a new product', () => {
    const input = document.createElement('input');
    input.scrollIntoView = jasmine.createSpy('scrollIntoView');
    spyOn(input, 'focus');

    component.focusNewProductForm(input);

    expect(input.scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth', block: 'center' });
    expect(input.focus).toHaveBeenCalled();
  });

  it('creates an inventory through the API and selects it for product creation', () => {
    const request: CreateInventoryRequest = {
      name: 'Estoque da loja',
      location: 'Loja principal',
      category: 'Geral',
      description: 'Produtos para venda'
    };
    const createdInventory: Inventory = {
      id: 8,
      inventoryCode: 'INV-8',
      ...request
    };
    inventoryService.createInventory.and.returnValue(of(createdInventory));
    component.isCreateInventoryModalOpen = true;

    component.createInventory(request);

    expect(inventoryService.createInventory).toHaveBeenCalledWith(request);
    expect(component.inventories).toEqual([createdInventory]);
    expect(component.selectedInventoryId).toBe(8);
    expect(component.isCreateInventoryModalOpen).toBeFalse();
    expect(component.isSavingInventory).toBeFalse();
  });

  it('updates and deletes an inventory and removes its products locally', () => {
    const inventory: Inventory = { id: 8, name: 'Estoque', location: 'Loja', category: 'Geral' };
    const updatedInventory = { ...inventory, name: 'Loja principal' };
    inventoryService.updateInventory.and.returnValue(of(updatedInventory));
    inventoryService.deleteInventory.and.returnValue(of(undefined));
    component.inventories = [inventory];
    component.inventoryBeingEdited = inventory;
    component.inventoryItems = [
      { id: 42, productCode: null, productName: 'Cadeira', category: 'Móveis', quantity: 1, minStockLevel: 1, maxStockLevel: 5, unitPrice: 120, inventoryId: 8, name: 'Cadeira', code: null, minStock: 1, maxStock: 5, barcode: null }
    ];

    component.createInventory({ name: 'Loja principal', location: 'Loja', category: 'Geral' });
    expect(inventoryService.updateInventory).toHaveBeenCalledWith(8, {
      name: 'Loja principal', location: 'Loja', category: 'Geral'
    });
    expect(component.inventories[0].name).toBe('Loja principal');

    component.deleteInventory(updatedInventory);

    expect(inventoryService.deleteInventory).toHaveBeenCalledWith(8);
    expect(component.inventories).toEqual([]);
    expect(component.inventoryItems).toEqual([]);
    expect(component.selectedInventoryId).toBeNull();
  });

  it('creates an item without a barcode and allows adding the barcode later', () => {
    const createdItem: InventoryItem = {
      id: 42,
      productCode: null,
      productName: 'Cadeira',
      category: 'Móveis',
      quantity: 1,
      minStockLevel: 5,
      maxStockLevel: 20,
      unitPrice: 120,
      inventoryId: 1
    };
    inventoryService.createItem.and.returnValue(of(createdItem));
    inventoryService.updateItem.and.returnValue(of({ ...createdItem, productCode: '1234567890123' }));
    component.inventories = [{ id: 1, name: 'Principal', location: 'Loja', category: 'Geral' }];
    component.selectedInventoryId = 1;
    component.newProduct.name = 'Cadeira';

    component.addInventoryProduct();

    expect(inventoryService.createItem).toHaveBeenCalledWith(1, jasmine.objectContaining({
      productCode: null,
      productName: 'Cadeira'
    }));
    expect(component.inventories[0].totalItems).toBe(1);
    expect(component.inventories[0].totalValue).toBe(120);

    const item = component.inventoryItems[0];
    component.editBarcode(item);
    component.editedBarcode = ' 1234567890123 ';
    component.saveBarcode(item);

    expect(inventoryService.updateItem).toHaveBeenCalledWith(42, jasmine.objectContaining({
      productCode: '1234567890123'
    }));
    expect(component.inventoryItems[0].productCode).toBe('1234567890123');
  });

  it('updates and deletes a product', () => {
    const inventory = { id: 1, name: 'Principal', location: 'Loja', category: 'Geral' };
    const item: InventoryItem = {
      id: 42,
      productCode: null,
      productName: 'Cadeira',
      category: 'Móveis',
      quantity: 1,
      minStockLevel: 1,
      maxStockLevel: 5,
      unitPrice: 120,
      inventoryId: 1
    };
    inventoryService.updateItem.and.returnValue(of({ ...item, productName: 'Cadeira executiva' }));
    inventoryService.deleteItem.and.returnValue(of(undefined));
    component.inventories = [inventory];
    component.inventoryItems = [{
      ...item,
      code: null,
      name: item.productName,
      minStock: item.minStockLevel,
      maxStock: item.maxStockLevel,
      barcode: null
    }];

    component.editInventoryProduct(component.inventoryItems[0]);
    component.newProduct.name = 'Cadeira executiva';
    component.addInventoryProduct();

    expect(inventoryService.updateItem).toHaveBeenCalledWith(42, jasmine.objectContaining({
      productName: 'Cadeira executiva',
      inventoryId: 1
    }));
    expect(component.inventoryItems[0].productName).toBe('Cadeira executiva');
    expect(component.inventories[0].totalItems).toBe(1);
    expect(component.inventories[0].totalValue).toBe(120);

    component.deleteInventoryProduct(component.inventoryItems[0]);

    expect(inventoryService.deleteItem).toHaveBeenCalledWith(42);
    expect(component.inventoryItems).toEqual([]);
  });

  it('creates the main inventory before adding a product when no inventory exists', () => {
    const inventory = { id: 9, name: 'Estoque principal', location: 'Loja', category: 'Geral' };
    const createdItem: InventoryItem = {
      id: 42,
      productCode: null,
      productName: 'Cadeira',
      category: 'Móveis',
      quantity: 1,
      minStockLevel: 5,
      maxStockLevel: 20,
      unitPrice: 120,
      inventoryId: 9
    };
    inventoryService.createInventory.and.returnValue(of(inventory));
    inventoryService.createItem.and.returnValue(of(createdItem));
    component.newProduct.name = 'Cadeira';

    component.addInventoryProduct();

    expect(inventoryService.createInventory).toHaveBeenCalledWith({
      name: 'Estoque principal',
      location: 'Loja',
      category: 'Geral'
    });
    expect(inventoryService.createItem).toHaveBeenCalledWith(9, jasmine.objectContaining({
      productName: 'Cadeira'
    }));
    expect(component.inventories[0]).toEqual(jasmine.objectContaining(inventory));
    expect(component.inventories[0].totalItems).toBe(1);
    expect(component.inventories[0].totalValue).toBe(120);
    expect(component.inventoryItems[0].productName).toBe('Cadeira');
  });
});
