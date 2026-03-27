import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerStats } from '../../../../services/customer.service';

@Component({
  selector: 'app-customer-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-header.component.html'
})
export class CustomerHeaderComponent {
  @Input() customerStats!: CustomerStats;
  @Input() isLoading = false;
  @Output() createNew = new EventEmitter<void>();
}