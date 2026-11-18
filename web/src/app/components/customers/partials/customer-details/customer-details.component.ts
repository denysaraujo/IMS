import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Customer } from '../../../../services/customer.service';

@Component({
  selector: 'app-customer-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-details.component.html'
})
export class CustomerDetailsComponent {
  @Input() customer!: Customer;
  @Output() edit = new EventEmitter<void>();
  @Output() delete = new EventEmitter<Customer>();
  @Output() close = new EventEmitter<void>();

  getCustomerTypeText(type: string): string {
    return type === 'INDIVIDUAL' ? 'Pessoa Física' : 'Pessoa Jurídica';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value || 0);
  }
}