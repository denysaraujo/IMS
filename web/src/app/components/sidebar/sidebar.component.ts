import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  imports: [CommonModule, RouterModule],
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Input() isMobileOpen = false;
  @Output() mobileClose = new EventEmitter<void>();

  // Menu items dinâmicos
  menuItems = [
    {
      label: 'Home',
      path: '/dashboard',
      icon: 'fas fa-house',
      badge: null
    },
    {
      label: 'Vendas',
      path: '/sales',
      icon: 'fas fa-shopping-cart',
      badge: null
    },
    {
      label: 'Locações',
      path: '/rentals',
      icon: 'fas fa-hand-holding-box',
      badge: null
    },
    {
      label: 'Configurações',
      path: '/settings',
      icon: 'fas fa-cog',
      badge: null,
      isExpanded: false,
      children: [
        {
          label: 'Clientes',
          path: '/customers',
          icon: 'fas fa-user',
          badge: null
        },
        {
          label: 'Estoque',
          path: '/inventory',
          icon: 'fas fa-boxes',
          badge: null
        },
        {
          label: 'Usuários',
          path: '/users',
          icon: 'fas fa-users',
          badge: null
        },
        {
          label: 'Relatórios',
          path: '/reports',
          icon: 'fas fa-chart-bar',
          badge: null
        },
        {
          label: 'Empresa',
          path: '/company',
          icon: 'fas fa-building',
          badge: null,
          roles: ['ADMIN']
        }
      ]
    },
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    const role = this.authService.getCurrentUser()?.role?.toUpperCase();
    const settings = this.menuItems.find(item => item.children);
    if (settings?.children) {
      settings.children = settings.children.filter(child => !child.roles || child.roles.includes(role || ''));
      settings.isExpanded = role === 'ADMIN';
    }
  }

  // Alternar expansão do submenu
  toggleSubmenu(item: any): void {
    if (item.children) {
      item.isExpanded = !item.isExpanded;
    }
  }

  // Fechar sidebar mobile
  closeMobileSidebar() {
    this.mobileClose.emit();
  }

  // Fechar sidebar mobile ao clicar em um item
  onItemClick() {
    this.closeMobileSidebar();
  }
}
