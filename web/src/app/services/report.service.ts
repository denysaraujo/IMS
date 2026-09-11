import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SalesReport {
  period: string;
  totalSales: number;
  totalRevenue: number;
  averageSaleValue: number;
  totalItemsSold: number;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly apiUrl = '/api/reports';

  constructor(private http: HttpClient) {}

  getDailySalesReport(date: string): Observable<SalesReport> {
    const params = new HttpParams().set('date', date);
    return this.http.get<SalesReport>(`${this.apiUrl}/sales/daily`, { params });
  }

  getMonthlySalesReport(year: number, month: number): Observable<SalesReport> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<SalesReport>(`${this.apiUrl}/sales/monthly`, { params });
  }
}
