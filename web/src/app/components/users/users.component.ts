import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService, User } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  
  // Modal states
  isNewUserModalOpen = false;
  isEditUserModalOpen = false;
  isViewUserModalOpen = false;
  isDeleteUserModalOpen = false;
  
  // User objects
  newUser: User = {
    nomeCompleto: '',
    username: '',
    password: '',
    role: 'USER',
    email: '',
    telefone: '',
    celular: '',
    enabled: true
  };

  editUser: User = {
    nomeCompleto: '',
    username: '',
    password: '', 
    role: 'USER',
    email: '',
    telefone: '',
    celular: '',
    enabled: true,
    active: true
  };

  selectedUser: User | null = null;
  confirmPassword = '';

  roles = [
    { value: 'USER', label: 'Usuário' },
    { value: 'MANAGER', label: 'Gerente' },
    { value: 'ADMIN', label: 'Administrador' },
    { value: 'SUPERVISOR', label: 'Supervisor' }
  ];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Verifica se o usuário está autenticado
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    
    // console.log('🔐 Usuário autenticado:', this.authService.getCurrentUser());
    // console.log('🔐 Token:', this.authService.getToken());
    
    this.loadUsers();
  }

  loadUsers() {
    // console.log('🔄 Carregando usuários...');
    
    this.userService.getUsers().subscribe({
      next: (users) => {
        // console.log('✅ Usuários carregados:', users);
        this.users = users;
      },
      error: (error) => {
        // console.error('❌ Erro ao carregar usuários:', error);
        if (error.status === 401) {
          alert('Sessão expirada. Faça login novamente.');
          this.authService.logout();
        } else {
          alert('Erro ao carregar usuários.');
        }
      }
    });
  }

  // Modal methods
  openNewUserModal() {
    this.isNewUserModalOpen = true;
    this.resetNewUser();
  }

  closeNewUserModal() {
    this.isNewUserModalOpen = false;
    this.resetNewUser();
  }

  openEditUserModal(user: User) {
    this.selectedUser = user;
    this.editUser = { 
      ...user,
      active: user.enabled // Mapeia enabled para active para o template
    };
    this.isEditUserModalOpen = true;
  }

  closeEditUserModal() {
    this.isEditUserModalOpen = false;
    this.selectedUser = null;
  }

  openViewUserModal(user: User) {
    this.selectedUser = user;
    this.isViewUserModalOpen = true;
  }

  closeViewUserModal() {
    this.isViewUserModalOpen = false;
    this.selectedUser = null;
  }

  openDeleteUserModal(user: User) {
    this.selectedUser = user;
    this.isDeleteUserModalOpen = true;
  }

  closeDeleteUserModal() {
    this.isDeleteUserModalOpen = false;
    this.selectedUser = null;
  }

  // CRUD operations
  createUser() {
    // Validação de senha
    if (this.newUser.password !== this.confirmPassword) {
      alert('As senhas não coincidem!');
      return;
    }

    this.userService.createUser(this.newUser).subscribe({
      next: (createdUser) => {
        this.users.push(createdUser);
        this.closeNewUserModal();
        alert('Usuário criado com sucesso!');
      },
      error: (error) => {
        console.error('Erro ao criar usuário', error);
        alert('Erro ao criar usuário. Verifique os dados e tente novamente.');
      }
    });
  }

  updateUser() {
    if (!this.selectedUser) return;

    // Mapeia active para enabled
    this.editUser.enabled = this.editUser.active;

    // Se não preencheu senha, remove o campo para não atualizar
    if (!this.editUser.password || this.editUser.password.trim() === '') {
        const { password, ...userWithoutPassword } = this.editUser;
        this.editUser = userWithoutPassword as User;
    }

    // Define os campos de estado da conta (valores padrão)
    this.editUser.accountNonLocked = true;
    this.editUser.accountNonExpired = true;
    this.editUser.credentialsNonExpired = true;

    this.userService.updateUser(this.selectedUser.id!, this.editUser).subscribe({
        next: (updatedUser) => {
            const index = this.users.findIndex(u => u.id === updatedUser.id);
            if (index !== -1) {
                this.users[index] = updatedUser;
            }
            this.closeEditUserModal();
            alert('Usuário atualizado com sucesso!');
        },
        error: (error) => {
            console.error('Erro ao atualizar usuário', error);
            alert('Erro ao atualizar usuário. Verifique os dados e tente novamente.');
        }
    });
  }

  deleteUser() {
    if (!this.selectedUser) return;

    this.userService.deleteUser(this.selectedUser.id!).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== this.selectedUser!.id);
        this.closeDeleteUserModal();
        alert('Usuário excluído com sucesso!');
      },
      error: (error) => {
        console.error('Erro ao excluir usuário', error);
        alert('Erro ao excluir usuário. Tente novamente.');
      }
    });
  }

  // Utility methods
  resetNewUser() {
    this.newUser = {
      nomeCompleto: '',
      username: '',
      password: '',
      role: 'USER',
      email: '',
      telefone: '',
      celular: '',
      enabled: true
    };
    this.confirmPassword = '';
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      case 'MANAGER': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'SUPERVISOR': return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
      case 'USER': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    }
  }

  getStatusBadgeClass(active: boolean): string {
    return active 
      ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
      : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
  }

  getFormattedRole(role: string): string {
    const roleObj = this.roles.find(r => r.value === role);
    return roleObj ? roleObj.label : role;
  }

  // Helper para o template - mapeia enabled para active
  isUserActive(user: User): boolean {
    return user.enabled ?? true;
  }

  // Método para obter a inicial do usuário
  getInitial(user: User): string {
    if (user.nomeCompleto && user.nomeCompleto.length > 0) {
      return user.nomeCompleto.charAt(0).toUpperCase();
    }
    if (user.username && user.username.length > 0) {
      return user.username.charAt(0).toUpperCase();
    }
    return '?';
  }

  // Formata data para exibição
  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    try {
      return new Date(dateString).toLocaleString('pt-BR');
    } catch (error) {
      return dateString;
    }
  }
}