import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Company, CompanyService } from '../../services/company.service';

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

  constructor(private companyService: CompanyService) {}

  ngOnInit(): void {
    this.companyService.load().subscribe({
      next: company => {
        this.company = { ...this.company, ...company };
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message = 'Não foi possível carregar os dados da empresa.';
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
    this.companyService.save(this.company).subscribe({
      next: saved => {
        this.company = saved;
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
