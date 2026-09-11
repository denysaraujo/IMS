import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Company {
  id?: number;
  name: string;
  document?: string;
  email?: string;
  phone?: string;
  address?: string;
  city?: string;
  state?: string;
  logoData?: string;
  printFooter?: string;
}

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly apiUrl = `${environment.apiUrl}/company`;
  private readonly companySubject = new BehaviorSubject<Company | null>(null);
  readonly company$ = this.companySubject.asObservable();

  constructor(private http: HttpClient) {}

  load(): Observable<Company> {
    return this.http.get<Company>(this.apiUrl).pipe(
      tap(company => this.companySubject.next(company))
    );
  }

  save(company: Company): Observable<Company> {
    return this.http.put<Company>(this.apiUrl, company).pipe(
      tap(savedCompany => this.companySubject.next(savedCompany))
    );
  }

  getCurrent(): Company | null {
    return this.companySubject.value;
  }
}
