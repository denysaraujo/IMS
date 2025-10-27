import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  imports: [CommonModule, RouterModule],
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
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
      label: 'Configurações', 
      path: '/settings', 
      icon: 'fas fa-cog',
      badge: null,
      isExpanded: false,
      children: [
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
        }
      ] 
    },
  ];

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