import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface InventoryItem {
  id: number;
  productCode: string;
  productName: string;
  quantity: number;
  minStockLevel: number;
  unitPrice: number;
  lowStockAlert: boolean;
  expirationDate?: string;
}

export interface AlertSummary {
  lowStockAlerts: number;
  expiringProducts: number;
}

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private apiUrl = '/api/alerts';

  constructor(private http: HttpClient) { }

  getLowStockAlerts(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.apiUrl}/low-stock`);
  }

  getAlertSummary(): Observable<AlertSummary> {
    return this.http.get<AlertSummary>(`${this.apiUrl}/summary`);
  }

  getExpiringProducts(days: number = 7): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.apiUrl}/expiring?days=${days}`);
  }
}