import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AlertService, AlertSummary, InventoryItem } from '../../services/alert.service';

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
  alertSummary: AlertSummary = { lowStockAlerts: 0, expiringProducts: 0 };
  lowStockAlerts: InventoryItem[] = [];

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
    private alertService: AlertService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUserData();
    this.loadAlerts();
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
    
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  loadAlerts() {
    // Carregar resumo de alertas
    this.alertService.getAlertSummary().subscribe({
      next: (summary) => {
        this.alertSummary = summary;
        this.stats[3].value = (summary.lowStockAlerts + summary.expiringProducts).toString();
      },
      error: (error) => console.error('Erro ao carregar alertas:', error)
    });

    // Carregar alertas de estoque baixo
    this.alertService.getLowStockAlerts().subscribe({
      next: (alerts) => {
        this.lowStockAlerts = alerts;
      },
      error: (error) => console.error('Erro ao carregar alertas de estoque:', error)
    });
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

  // Navegação para estoque
  navigateToInventory() {
    this.router.navigate(['/inventory']);
  }

  // Navegação para vendas
  navigateToSales() {
    this.router.navigate(['/sales']);
  }
}