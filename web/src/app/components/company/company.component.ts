import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Company, CompanyService } from '../../services/company.service';
import { CepService } from '../../services/cep.service';

@Component({
  selector: 'app-company',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company.component.html'
})
export class CompanyComponent implements OnInit {
  company: Company = { name: '', printFooter: '' };
  loading = true;
  saving = false;
  message = '';
  zipCodeLookupError = '';

  constructor(
    private companyService: CompanyService,
    private cepService: CepService
  ) {}

  ngOnInit(): void {
    this.companyService.load().subscribe({
      next: company => {
        this.company = { ...this.company, ...company };
        this.normalizeCompanyAddress();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message = 'Não foi possível carregar os dados da empresa.';
      }
    });
  }

  private normalizeCompanyAddress(): void {
    this.company.address = this.company.address ?? '';
    this.company.addressNumber = this.company.addressNumber ?? '';
    this.company.neighborhood = this.company.neighborhood ?? '';
    this.company.zipCode = this.company.zipCode ?? '';
  }

  formatDocument(value: string): string {
    const digits = (value ?? '').replace(/\D/g, '');

    if (digits.length <= 11) {
      const cpf = digits.slice(0, 11);
      if (cpf.length <= 3) return cpf;
      if (cpf.length <= 6) return cpf.replace(/(\d{3})(\d{0,3})/, '$1.$2');
      if (cpf.length <= 9) return cpf.replace(/(\d{3})(\d{3})(\d{0,3})/, '$1.$2.$3');
      return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{0,2})/, '$1.$2.$3-$4');
    }

    const cnpj = digits.slice(0, 14);
    if (cnpj.length <= 2) return cnpj;
    if (cnpj.length <= 5) return cnpj.replace(/(\d{2})(\d{0,3})/, '$1.$2');
    if (cnpj.length <= 8) return cnpj.replace(/(\d{2})(\d{3})(\d{0,3})/, '$1.$2.$3');
    if (cnpj.length <= 12) return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{0,4})/, '$1.$2.$3/$4');
    return cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{0,2})/, '$1.$2.$3/$4-$5');
  }

  formatPhone(value: string): string {
    const digits = (value ?? '').replace(/\D/g, '').slice(0, 11);

    if (!digits) return '';
    if (digits.length <= 2) return `(${digits}`;
    if (digits.length <= 6) return digits.replace(/(\d{2})(\d{0,4})/, '($1) $2');
    if (digits.length <= 10) return digits.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
    return digits.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
  }

  formatZipCode(zipCode: string): string {
    const digits = (zipCode ?? '').replace(/\D/g, '').slice(0, 8);
    if (digits.length <= 5) {
      return digits;
    }
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
  }

  onDocumentInput(value: string): void {
    this.company.document = this.formatDocument(value);
  }

  onPhoneInput(value: string): void {
    this.company.phone = this.formatPhone(value);
  }

  onZipCodeInput(value: string): void {
    this.company.zipCode = this.formatZipCode(value);
    this.zipCodeLookupError = '';

    const digits = this.company.zipCode.replace(/\D/g, '');
    if (digits.length !== 8) {
      return;
    }

    this.cepService.lookup(digits).subscribe({
      next: address => {
        this.company.address = address.street || this.company.address || '';
        this.company.neighborhood = address.neighborhood || this.company.neighborhood || '';
        this.company.city = address.city || this.company.city || '';
        this.company.state = address.state || this.company.state || '';
      },
      error: () => {
        this.zipCodeLookupError = 'Não foi possível consultar o CEP informado.';
      }
    });
  }

  onLogoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/') || file.size > 2 * 1024 * 1024) {
      this.message = 'Selecione uma imagem de até 2 MB.';
      input.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => this.company.logoData = String(reader.result);
    reader.readAsDataURL(file);
  }

  save(): void {
    this.saving = true;
    this.message = '';
    this.company.zipCode = this.formatZipCode(this.company.zipCode ?? '');
    this.companyService.save(this.company).subscribe({
      next: saved => {
        this.company = saved;
        this.normalizeCompanyAddress();
        this.saving = false;
        this.message = 'Dados da empresa salvos. Os próximos impressos usarão esta identidade.';
      },
      error: () => {
        this.saving = false;
        this.message = 'Não foi possível salvar os dados da empresa.';
      }
    });
  }
}
