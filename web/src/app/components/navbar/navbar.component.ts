import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  imports: [CommonModule, RouterModule],
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isDarkMode = false;
  isMenuOpen = false;
  isSettingsOpen = false; // Novo estado para dropdown de configurações
  
  // Menu items dinâmicos - removidos Cadastro de Produtos e Estoque
  menuItems = [
    { label: 'Home', path: '/home', icon: 'fas fa-house' },
    { label: 'Vendas', path: '/sales', icon: 'fas fa-hand-holding-dollar' }
  ]

  // Subitens de configurações
  settingsItems = [
    { label: 'Configurações de Usuário', path: '/users', icon: 'fas fa-users' },
    { label: 'Estoque', path: '/inventory', icon: 'fas fa-boxes' },
    { label: 'Relatórios', path: '/reports', icon: 'fas fa-chart-bar' }
  ];

  // Company info dinâmica
  companyInfo = {
    name: 'Duo Technology',
    logo: {
      text: 'Logo',
      image: null // ou '/assets/logo.png'
    }
  };
  currentUser: any;

  constructor(
    @Inject(PLATFORM_ID) 
    private platformId: any,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Verificar tema salvo apenas no cliente
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'dark') {
        this.isDarkMode = true;
        document.documentElement.classList.add('dark');
      }
    }

    // Carregar dados do usuário
    this.loadUserData();
  }

  loadUserData() {
    this.currentUser = this.authService.getCurrentUser();
    console.log('👤 Usuário no navbar:', this.currentUser);
  }

  toggleDarkMode() {
    this.isDarkMode = !this.isDarkMode;
    
    // Executar apenas no cliente
    if (isPlatformBrowser(this.platformId)) {
      const htmlElement = document.documentElement;
      if (this.isDarkMode) {
        htmlElement.classList.add('dark');
        localStorage.setItem('theme', 'dark');
      } else {
        htmlElement.classList.remove('dark');
        localStorage.setItem('theme', 'light');
      }
    }
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  toggleSettings() {
    this.isSettingsOpen = !this.isSettingsOpen;
  }

  closeMenu() {
    this.isMenuOpen = false;
    this.isSettingsOpen = false;
  }

  // Método para fazer logout
  logout() {
    this.authService.logout();
    this.closeMenu();
  }

  // Método para formatar o nome de exibição
  getDisplayName(): string {
    if (!this.currentUser) {
      this.loadUserData();
    }

    if (this.currentUser?.nomeCompleto) {
      return this.currentUser.nomeCompleto;
    }
    if (this.currentUser?.username) {
      return this.currentUser.username.charAt(0).toUpperCase() + 
             this.currentUser.username.slice(1);
    }
    return 'Usuário';
  }

  // Método para obter a role formatada
  getFormattedRole(): string {
    if (!this.currentUser) {
      this.loadUserData();
    }

    const role = this.currentUser?.role;
    switch(role) {
      case 'ADMIN': return 'Administrador';
      case 'USER': return 'Usuário';
      case 'MANAGER': return 'Gerente';
      default: return role || 'Usuário';
    }
  }

  // Navegar para uma rota e fechar menus
  navigateTo(path: string) {
    this.router.navigate([path]);
    this.closeMenu();
  }
}