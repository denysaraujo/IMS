import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Customer } from '../../../../services/customer.service';
import { ESTADOS_BRASIL, DOCUMENT_LIMITS, PHONE_LIMITS } from '../../customers.constants';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-form.component.html'
})
export class CustomerFormComponent implements OnInit {
  @Input() customer!: Customer;
  @Input() isCreating = false;
  @Input() isEditing = false;
  @Input() isLoading = false;
  @Output() save = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @Output() documentChange = new EventEmitter<void>();
  @Output() phoneInput = new EventEmitter<Event>();
  @Output() zipCodeChange = new EventEmitter<void>();

  estados = ESTADOS_BRASIL;
  readonly DOCUMENT_LIMITS = DOCUMENT_LIMITS;
  readonly PHONE_LIMITS = PHONE_LIMITS;

  ngOnInit() {
    if (!this.customer.address) {
      this.customer.address = {
        street: '',
        number: '',
        complement: '',
        neighborhood: '',
        city: '',
        state: '',
        zipCode: '',
        country: 'Brasil'
      };
    }
  }

  onDocumentTypeChange() {
    this.documentChange.emit();
  }

  // CORRIGIDO: Agora aceita o parâmetro $event
  onDocumentInput(event: Event) {
    this.documentChange.emit();
  }

  // CORRIGIDO: Agora aceita o parâmetro $event
  onPhoneInput(event: Event) {
    this.phoneInput.emit(event);
  }

  formatZipCode() {
    this.zipCodeChange.emit();
  }
}