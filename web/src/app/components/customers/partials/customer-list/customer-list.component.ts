import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Customer } from '../../../../services/customer.service';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-list.component.html'
})
export class CustomerListComponent {
  @Input() customers: Customer[] = [];
  @Input() selectedCustomer: Customer | null = null;
  @Input() isLoading = false;
  @Input() searchTerm = '';
  @Output() searchChange = new EventEmitter<string>();
  @Output() selectCustomer = new EventEmitter<Customer>();
  @Output() editCustomer = new EventEmitter<Customer>();
  @Output() deleteCustomer = new EventEmitter<Customer>();

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value || 0);
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('pt-BR');
  }

  onSearchInput(value: string) {
    this.searchChange.emit(value);
  }

  onSelectCustomer(customer: Customer, event: Event) {
    event.stopPropagation();
    this.selectCustomer.emit(customer);
  }

  onEditCustomer(customer: Customer, event: Event) {
    event.stopPropagation();
    this.editCustomer.emit(customer);
  }

  onDeleteCustomer(customer: Customer, event: Event) {
    event.stopPropagation();
    this.deleteCustomer.emit(customer);
  }
}