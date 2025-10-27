import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, User } from '../../../services/user.service';

@Component({
  selector: 'app-delete-user-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-delete-modal.component.html',
  styleUrls: ['./user-delete-modal.component.css']
})
export class DeleteUserModalComponent {
  @Input() user: User | null = null;
  @Output() userDeleted = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  constructor(private userService: UserService) {}

  onConfirm() {
    if (this.user?.id) {
      this.userService.deleteUser(this.user.id).subscribe({
        next: () => {
          this.userDeleted.emit();
        },
        error: (error) => {
          console.error('Erro ao excluir usuário', error);
          alert('Erro ao excluir usuário. Tente novamente.');
        }
      });
    }
  }

  onCancel() {
    this.cancel.emit();
  }
}