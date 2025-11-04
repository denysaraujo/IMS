import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Customer {
  id?: number;
  name: string;
  email?: string;
  phone?: string;
  document: string;
  type: 'INDIVIDUAL' | 'COMPANY';
  address?: Address;
  createdAt?: string;
  updatedAt?: string;
  totalPurchases?: number;  
  totalOrders?: number;     
}

export interface Address {
  street?: string;
  number?: string;
  complement?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
}

export interface CustomerStats {
  totalCustomers: number;
  individualCustomers: number;
  companyCustomers: number;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) { }

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.apiUrl);
  }

  getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/${id}`);
  }

  createCustomer(customer: Customer): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, customer);
  }

  updateCustomer(id: number, customer: Customer): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, customer);
  }

  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchCustomers(name: string): Observable<Customer[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<Customer[]>(`${this.apiUrl}/search`, { params });
  }

  getCustomerStats(): Observable<CustomerStats> {
    return this.http.get<CustomerStats>(`${this.apiUrl}/stats`);
  }

  getTopCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/top`);
  }
}