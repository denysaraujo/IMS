import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '../../../services/user.service';

@Component({
  selector: 'app-view-user-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-view-modal.component.html',
  styleUrls: ['./user-view-modal.component.css']
})
export class ViewUserModalComponent {
  @Input() user: User | null = null;
  @Output() close = new EventEmitter<void>();

  getFormattedRole(role: string): string {
    switch(role) {
      case 'ADMIN': return 'Administrador';
      case 'MANAGER': return 'Gerente';
      case 'USER': return 'Usuário';
      default: return role;
    }
  }

  onClose() {
    this.close.emit();
  }
}