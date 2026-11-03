import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateInventoryRequest, Inventory } from '../../../services/inventory.service';

@Component({
  selector: 'app-inventory-create-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory-create-modal.component.html'
})
export class InventoryCreateModalComponent {
  @Input() isSaving = false;
  @Input() initialInventory: Inventory | null = null;
  @Output() inventorySaved = new EventEmitter<CreateInventoryRequest>();
  @Output() cancel = new EventEmitter<void>();

  inventory: CreateInventoryRequest = this.createEmptyInventory();

  ngOnInit(): void {
    if (this.initialInventory) {
      this.inventory = {
        name: this.initialInventory.name,
        location: this.initialInventory.location,
        category: this.initialInventory.category,
        description: this.initialInventory.description ?? '',
        status: this.initialInventory.status ?? 'ACTIVE'
      };
    }
  }

  onSubmit(): void {
    this.inventorySaved.emit({ ...this.inventory });
  }

  onCancel(): void {
    this.cancel.emit();
  }

  private createEmptyInventory(): CreateInventoryRequest {
    return {
      name: '',
      location: '',
      category: '',
      description: ''
    };
  }
}