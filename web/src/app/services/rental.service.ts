import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from './customer.service';

export interface Rental {
  id?: number;
  rentalCode: string;
  customer: Customer;
  productCode: string;
  productName: string;
  quantity: number;
  dailyRate: number;
  rentalDays: number;
  startDate: string;
  expectedReturnDate?: string;
  actualReturnDate?: string;
  totalAmount: number;
  depositAmount?: number;
  status: 'ACTIVE' | 'RETURNED' | 'OVERDUE';
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class RentalService {
  private readonly apiUrl = '/api/rentals';

  constructor(private http: HttpClient) {}

  getRentals(): Observable<Rental[]> {
    return this.http.get<Rental[]>(this.apiUrl);
  }

  createRental(rental: Rental): Observable<Rental> {
    return this.http.post<Rental>(this.apiUrl, rental);
  }

  returnRental(id: number): Observable<Rental> {
    return this.http.put<Rental>(`${this.apiUrl}/${id}/return`, {});
  }
}
