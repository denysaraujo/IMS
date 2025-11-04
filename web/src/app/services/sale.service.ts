import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from './customer.service';

export interface Sale {
  id?: number;
  saleCode: string;    
  saleDate: string;   
  totalAmount: number; 
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  notes?: string;
  customer: Customer;  
  items: SaleItem[];
}

export interface SaleItem {
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface SaleCustomer {
  id?: number;
  name: string;
  email?: string;
  document: string;
}

export interface SalesReport {
  period: string;
  totalSales: number;
  totalRevenue: number;
  averageSale: number;
  totalItemsSold: number;
}

@Injectable({
  providedIn: 'root'
})
export class SaleService {
  private apiUrl = '/api/sales';

  constructor(private http: HttpClient) { }

  createSale(sale: Sale): Observable<Sale> {
    return this.http.post<Sale>(this.apiUrl, sale);
  }

  getSales(): Observable<Sale[]> {
    return this.http.get<Sale[]>(this.apiUrl);
  }

  getSale(id: number): Observable<Sale> {
    return this.http.get<Sale>(`${this.apiUrl}/${id}`);
  }

  getSalesByPeriod(startDate: string, endDate: string): Observable<Sale[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<Sale[]>(`${this.apiUrl}/period`, { params });
  }

  getSalesByCustomer(customerId: number): Observable<Sale[]> {
    return this.http.get<Sale[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getSalesReport(startDate: string, endDate: string): Observable<SalesReport[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<SalesReport[]>(`${this.apiUrl}/reports`, { params });
  }

  cancelSale(saleId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${saleId}/cancel`, {});
  }
}