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
      label: 'Cadastro de Produtos', 
      path: '/product_registration', 
      icon: 'fas fa-check',
      badge: null 
    },
    { 
      label: 'Vendas', 
      path: '/sales', 
      icon: 'fas fa-hand-holding-dollar',
      // badge: 5 
    },
    { 
      label: 'Estoque', 
      path: '/stock', 
      icon: 'fas fa-paste',
      // badge: 'New' 
    },
    { 
      label: 'Configurações', 
      path: '/settings', 
      icon: 'fas fa-cog',
      badge: null 
    },
  ];

  closeMobileSidebar() {
    this.mobileClose.emit();
  }

  isNumberBadge(badge: any): boolean {
    return typeof badge === 'number';
  }
}