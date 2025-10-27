import { Component, EventEmitter, Input, Output, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, User } from '../../../services/user.service';

@Component({
  selector: 'app-edit-user-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-edit-modal.component.html',
  styleUrls: ['./user-edit-modal.component.css']
})
export class EditUserModalComponent implements OnChanges {
  @Input() user: User | null = null;
  @Output() userUpdated = new EventEmitter<User>();
  @Output() cancel = new EventEmitter<void>();

  editedUser: User = {
    nomeCompleto: '',
    username: '',
    password: '', // Mantemos password, mas será opcional na edição
    role: 'USER',
    email: '',
    telefone: '',
    celular: '',
    enabled: true
  };

  roles = [
    { value: 'USER', label: 'Usuário' },
    { value: 'MANAGER', label: 'Gerente' },
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'SUPERVISOR', label: 'Supervisor' }
  ];

  isLoading = false;

  constructor(private userService: UserService) {}

  ngOnChanges() {
    if (this.user) {
      console.log('📝 Editando usuário:', this.user);
      // Copia o usuário, mas mantém a senha vazia por segurança
      this.editedUser = { 
        ...this.user,
        password: '' // Não preenchemos a senha por segurança
      };
    }
  }

  onSubmit() {
    if (this.user?.id) {
      this.isLoading = true;
      
      console.log('🔄 Iniciando atualização do usuário ID:', this.user.id);
      console.log('📤 Dados a serem enviados:', this.editedUser);

      // Prepara os dados para envio
      const userToUpdate = { ...this.editedUser };
      
      // Se a senha estiver vazia, remove do objeto para não atualizar
      if (!userToUpdate.password || userToUpdate.password.trim() === '') {
        console.log('🔒 Senha vazia, removendo do payload');
        delete userToUpdate.password;
      } else {
        console.log('🔒 Nova senha fornecida, será atualizada');
      }

      this.userService.updateUser(this.user.id, userToUpdate).subscribe({
        next: (updatedUser) => {
          console.log('✅ Usuário atualizado com sucesso:', updatedUser);
          this.isLoading = false;
          this.userUpdated.emit(updatedUser);
        },
        error: (error) => {
          console.error('❌ Erro ao atualizar usuário:', error);
          console.error('❌ Status:', error.status);
          console.error('❌ URL:', error.url);
          console.error('❌ Mensagem:', error.message);
          this.isLoading = false;
          
          if (error.status === 403) {
            alert('Acesso negado. Você não tem permissão para editar usuários.');
          } else if (error.status === 404) {
            alert('Usuário não encontrado.');
          } else {
            alert('Erro ao atualizar usuário. Verifique os dados e tente novamente.');
          }
        }
      });
    }
  }

  onCancel() {
    this.cancel.emit();
  }
}