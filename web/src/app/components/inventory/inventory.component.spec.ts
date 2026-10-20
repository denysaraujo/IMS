import { of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { InventoryItem, InventoryService } from '../../services/inventory.service';
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
      'createItem',
      'updateItem'
    ]);
    component = new InventoryComponent(authService, router, inventoryService);
    spyOn(window, 'alert');
  });

  it('takes focus to the product form when starting a new product', () => {
    const input = document.createElement('input');
    input.scrollIntoView = jasmine.createSpy('scrollIntoView');
    spyOn(input, 'focus');

    component.focusNewProductForm(input);

    expect(input.scrollIntoView).toHaveBeenCalledWith({ behavior: 'smooth', block: 'center' });
    expect(input.focus).toHaveBeenCalled();
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
      unitPrice: 120
    };
    inventoryService.createItem.and.returnValue(of(createdItem));
    inventoryService.updateItem.and.returnValue(of({ ...createdItem, productCode: '1234567890123' }));
    component.inventories = [{ id: 1, name: 'Principal', location: 'Loja', category: 'Geral' }];
    component.newProduct.name = 'Cadeira';

    component.addInventoryProduct();

    expect(inventoryService.createItem).toHaveBeenCalledWith(1, jasmine.objectContaining({
      productCode: null,
      productName: 'Cadeira'
    }));

    const item = component.inventoryItems[0];
    component.editBarcode(item);
    component.editedBarcode = ' 1234567890123 ';
    component.saveBarcode(item);

    expect(inventoryService.updateItem).toHaveBeenCalledWith(42, jasmine.objectContaining({
      productCode: '1234567890123'
    }));
    expect(component.inventoryItems[0].productCode).toBe('1234567890123');
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
      unitPrice: 120
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
    expect(component.inventories).toEqual([inventory]);
    expect(component.inventoryItems[0].productName).toBe('Cadeira');
  });
});
