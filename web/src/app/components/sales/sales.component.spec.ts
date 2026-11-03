import { of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CompanyService } from '../../services/company.service';
import { Customer, CustomerService } from '../../services/customer.service';
import { InventoryItem, InventoryService } from '../../services/inventory.service';
import { Sale, SaleService } from '../../services/sale.service';
import { SalesComponent } from './sales.component';

describe('SalesComponent', () => {
  let component: SalesComponent;
  let saleService: jasmine.SpyObj<SaleService>;
  let inventoryService: jasmine.SpyObj<InventoryService>;

  beforeEach(() => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['getCurrentUser']);
    saleService = jasmine.createSpyObj<SaleService>('SaleService', ['getSales', 'createSale', 'cancelSale']);
    const customerService = jasmine.createSpyObj<CustomerService>('CustomerService', ['getCustomers']);
    inventoryService = jasmine.createSpyObj<InventoryService>('InventoryService', ['getItems']);
    const companyService = jasmine.createSpyObj<CompanyService>('CompanyService', ['load']);
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    component = new SalesComponent(
      authService,
      saleService,
      customerService,
      inventoryService,
      companyService,
      router
    );
    spyOn(window, 'alert');
    spyOn(window, 'confirm').and.returnValue(true);
  });

  it('uses inventory product details and allows changing only the sale price', () => {
    const product: InventoryItem = {
      id: 21,
      productCode: null,
      productName: 'Cadeira',
      description: 'Cadeira ergonômica',
      category: 'Móveis',
      quantity: 5,
      minStockLevel: 1,
      maxStockLevel: 10,
      unitPrice: 120
    };
    component.inventoryProducts = [product];

    component.selectInventoryProduct(21);
    component.newItem.quantity = 2;
    component.newItem.unitPrice = 110;
    component.addItem();

    expect(component.newSale.items).toEqual([jasmine.objectContaining({
      inventoryItemId: 21,
      productName: 'Cadeira',
      category: 'Móveis',
      description: 'Cadeira ergonômica',
      quantity: 2,
      unitPrice: 110
    })]);
    expect(component.newSale.totalAmount).toBe(220);
  });

  it('rejects a sale quantity greater than the inventory quantity', () => {
    component.inventoryProducts = [{
      id: 21,
      productCode: 'P-21',
      productName: 'Cadeira',
      category: 'Móveis',
      quantity: 1,
      minStockLevel: 1,
      maxStockLevel: 10,
      unitPrice: 120
    }];
    component.selectInventoryProduct(21);
    component.newItem.quantity = 2;

    component.addItem();

    expect(component.newSale.items).toEqual([]);
    expect(window.alert).toHaveBeenCalled();
  });

  it('includes company contact details and sold products in the receipt', () => {
    const sale: Sale = {
      saleCode: 'V-1',
      saleDate: '2026-11-03T10:00:00',
      totalAmount: 220,
      status: 'COMPLETED',
      customer: { name: 'Cliente', document: '123', type: 'INDIVIDUAL' },
      items: [{
        productCode: null,
        productName: 'Cadeira',
        category: 'Móveis',
        description: 'Cadeira ergonômica',
        quantity: 2,
        unitPrice: 110
      }]
    };
    component.company = {
      name: 'Empresa Exemplo',
      document: '12.345.678/0001-90',
      address: 'Rua Central, 10',
      city: 'São Paulo',
      state: 'SP',
      phone: '(11) 99999-9999',
      email: 'contato@empresa.com'
    };

    const receipt = component.generateReceipt(sale);

    expect(receipt).toContain('CNPJ: 12.345.678/0001-90');
    expect(receipt).toContain('Endereço: Rua Central, 10, São Paulo, SP');
    expect(receipt).toContain('Telefone: (11) 99999-9999');
    expect(receipt).toContain('E-mail: contato@empresa.com');
    expect(receipt).toContain('Cadeira (Móveis)');
    expect(receipt).toContain('Cadeira ergonômica');
    expect(receipt).toContain('Total vendido:');
    expect(receipt).toContain('220,00');
  });

  it('submits the sale and reloads stock after success', () => {
    const customer: Customer = {
      id: 3,
      name: 'Cliente',
      document: '123',
      type: 'INDIVIDUAL'
    };
    const sale: Sale = {
      saleCode: 'V-1',
      saleDate: '2026-11-03T10:00:00',
      totalAmount: 110,
      status: 'COMPLETED',
      customer,
      items: [{
        inventoryItemId: 21,
        productCode: null,
        productName: 'Cadeira',
        category: 'Móveis',
        quantity: 1,
        unitPrice: 110
      }]
    };
    component.selectedCustomer = customer;
    component.newSale.items = sale.items;
    saleService.createSale.and.returnValue(of(sale));
    inventoryService.getItems.and.returnValue(of([]));

    component.processSale();

    expect(saleService.createSale).toHaveBeenCalled();
    expect(component.sales).toEqual([sale]);
    expect(inventoryService.getItems).toHaveBeenCalled();
    expect(component.isProcessingSale).toBeFalse();
  });

  it('does not process a sale when there is no selected customer', () => {
    component.newSale.items = [{
      inventoryItemId: 21,
      productCode: null,
      productName: 'Cadeira',
      quantity: 1,
      unitPrice: 110
    }];

    component.processSale();

    expect(saleService.createSale).not.toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Selecione um cliente para a venda');
  });

  it('cancels a completed sale and reloads stock', () => {
    const sale: Sale = {
      id: 8,
      saleCode: 'V-8',
      saleDate: '2026-11-03T10:00:00',
      totalAmount: 110,
      status: 'COMPLETED',
      customer: { id: 3, name: 'Cliente', document: '123', type: 'INDIVIDUAL' },
      items: []
    };
    component.sales = [sale];
    saleService.cancelSale.and.returnValue(of(undefined));
    inventoryService.getItems.and.returnValue(of([]));

    component.cancelSale(sale);

    expect(saleService.cancelSale).toHaveBeenCalledWith(8);
    expect(component.sales[0].status).toBe('CANCELLED');
    expect(inventoryService.getItems).toHaveBeenCalled();
  });
});
