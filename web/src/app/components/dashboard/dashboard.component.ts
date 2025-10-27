import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: any = null;
  lastLogin: string = '';

  stats = [
    { 
      title: 'Total de Usuários', 
      value: '0', 
      icon: '👥',
      trend: '+12%'
    },
    { 
      title: 'Pedidos Hoje', 
      value: '0', 
      icon: '🛒',
      trend: '+5%'
    },
    { 
      title: 'Mensagens', 
      value: '0', 
      icon: '💬',
      trend: '-2%'
    },
    { 
      title: 'Alertas', 
      value: '0', 
      icon: '⚠️',
      trend: '0%'
    },
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    this.lastLogin = new Date().toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
    
    // console.log('👤 Usuário atual:', this.currentUser);
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  getDisplayName(): string {
    if (this.currentUser?.nomeCompleto) {
      return this.currentUser.nomeCompleto;
    }
    if (this.currentUser?.username) {
      return this.currentUser.username.charAt(0).toUpperCase() + 
             this.currentUser.username.slice(1);
    }
    return 'Usuário';
  }

  getFormattedRole(): string {
    const role = this.currentUser?.role;
    switch(role) {
      case 'ADMIN': return 'Administrador';
      case 'USER': return 'Usuário';
      case 'MANAGER': return 'Gerente';
      case 'SUPERVISOR': return 'Supervisor';
      default: return role || 'Usuário';
    }
  }
}