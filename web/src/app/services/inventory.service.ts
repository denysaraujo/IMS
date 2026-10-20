import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Inventory {
  id?: number;
  inventoryCode?: string;
  name: string;
  description?: string;
  location: string;
  category: string;
  totalValue?: number;
  totalItems?: number;
  status?: string;
}

export interface InventoryItem {
  id?: number;
  productCode: string | null;
  productName: string;
  description?: string;
  category: string;
  quantity: number;
  minStockLevel: number;
  maxStockLevel: number;
  unitPrice: number;
  supplier?: string;
  location?: string;
  status?: string;
  expirationDate?: string;
  inventory?: Inventory;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly apiUrl = '/api/inventory';

  constructor(private http: HttpClient) {}

  getInventories(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(this.apiUrl);
  }

  createInventory(inventory: Pick<Inventory, 'name' | 'location' | 'category'> & Partial<Pick<Inventory, 'description'>>): Observable<Inventory> {
    return this.http.post<Inventory>(this.apiUrl, inventory);
  }

  getItems(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.apiUrl}/items`);
  }

  getItemByCode(productCode: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.apiUrl}/items/code/${encodeURIComponent(productCode)}`);
  }

  createItem(inventoryId: number, item: InventoryItem): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(`${this.apiUrl}/items/inventory/${inventoryId}`, item);
  }

  updateItem(id: number, item: InventoryItem): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.apiUrl}/items/${id}`, item);
  }

  updateStock(id: number, quantityChange: number): Observable<InventoryItem> {
    const params = new HttpParams().set('quantityChange', quantityChange);
    return this.http.patch<InventoryItem>(`${this.apiUrl}/items/${id}/stock`, null, { params });
  }
}
