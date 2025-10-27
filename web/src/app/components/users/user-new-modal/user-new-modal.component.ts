import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../../../services/user.service';

@Component({
  selector: 'app-new-user-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-new-modal.component.html'
})
export class NewUserModalComponent {
  @Output() userCreated = new EventEmitter<User>();
  @Output() cancel = new EventEmitter<void>();
  @Input() user: User = {
    nomeCompleto: '',
    username: '',
    password: '',
    role: 'USER',
    email: '',
    telefone: '',
    celular: '',
    enabled: true
  };

  @Input() confirmPassword: string = '';

  roles = [
    { value: 'USER', label: 'Usuário' },
    { value: 'MANAGER', label: 'Gerente' },
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'SUPERVISOR', label: 'Supervisor' }
  ];

  onSubmit() {
    this.userCreated.emit(this.user);
  }

  onCancel() {
    this.cancel.emit();
  }
}