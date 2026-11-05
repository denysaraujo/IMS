import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CepService } from './cep.service';

describe('CepService', () => {
  let service: CepService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CepService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTestingController.verify());

  it('looks up and maps the address returned for a CEP', () => {
    let address: unknown;
    service.lookup('01001-000').subscribe(result => address = result);

    const request = httpTestingController.expectOne('https://viacep.com.br/ws/01001000/json/');
    expect(request.request.method).toBe('GET');
    request.flush({
      logradouro: 'Praça da Sé',
      bairro: 'Sé',
      localidade: 'São Paulo',
      uf: 'SP'
    });

    expect(address).toEqual({
      street: 'Praça da Sé',
      neighborhood: 'Sé',
      city: 'São Paulo',
      state: 'SP'
    });
  });

  it('rejects invalid CEP values without making a request', () => {
    let error: Error | undefined;
    service.lookup('123').subscribe({ error: receivedError => error = receivedError });

    expect(error?.message).toBe('CEP deve conter 8 dígitos.');
    httpTestingController.expectNone('https://viacep.com.br/ws/123/json/');
  });
});
