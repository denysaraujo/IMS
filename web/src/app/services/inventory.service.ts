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

export type CreateInventoryRequest = Pick<Inventory, 'name' | 'location' | 'category'> &
  Partial<Pick<Inventory, 'description' | 'status'>>;

export type UpdateInventoryRequest = CreateInventoryRequest;

export interface InventoryItem {
  id?: number;
  productCode: string | null;
  productName: string;
  description?: string;
  category: string;
  quantity: number;
  availableForRental?: boolean;
  minStockLevel: number;
  maxStockLevel: number;
  unitPrice: number;
  supplier?: string;
  location?: string;
  status?: string;
  expirationDate?: string;
  inventoryId?: number;
  inventory?: Inventory;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly apiUrl = '/api/inventory';

  constructor(private http: HttpClient) {}

  getInventories(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(this.apiUrl);
  }

  createInventory(inventory: CreateInventoryRequest): Observable<Inventory> {
    return this.http.post<Inventory>(this.apiUrl, inventory);
  }

  updateInventory(id: number, inventory: UpdateInventoryRequest): Observable<Inventory> {
    return this.http.put<Inventory>(`${this.apiUrl}/${id}`, inventory);
  }

  deleteInventory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
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

  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/items/${id}`);
  }

  updateStock(id: number, quantityChange: number): Observable<InventoryItem> {
    const params = new HttpParams().set('quantityChange', quantityChange);
    return this.http.patch<InventoryItem>(`${this.apiUrl}/items/${id}/stock`, null, { params });
  }
}
