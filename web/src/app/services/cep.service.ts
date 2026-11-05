import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, throwError } from 'rxjs';

export interface CepAddress {
  street: string;
  neighborhood: string;
  city: string;
  state: string;
}

interface ViaCepResponse {
  logradouro?: string;
  bairro?: string;
  localidade?: string;
  uf?: string;
  erro?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CepService {
  private readonly viaCepUrl = 'https://viacep.com.br/ws';

  constructor(private http: HttpClient) {}

  lookup(zipCode: string): Observable<CepAddress> {
    const digits = zipCode.replace(/\D/g, '');
    if (digits.length !== 8) {
      return throwError(() => new Error('CEP deve conter 8 dígitos.'));
    }

    return this.http.get<ViaCepResponse>(`${this.viaCepUrl}/${digits}/json/`).pipe(
      map(response => {
        if (response.erro) {
          throw new Error('CEP não encontrado.');
        }

        return {
          street: response.logradouro || '',
          neighborhood: response.bairro || '',
          city: response.localidade || '',
          state: response.uf || ''
        };
      })
    );
  }
}
