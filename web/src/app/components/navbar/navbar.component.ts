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
  
  // Menu items dinâmicos
  menuItems = [
    { label: 'Home', path: '/home', icon: 'fas fa-house' },
    { label: 'Cadastro de Produtos', path: '/product_registration', icon: 'fas fa-check' },
    { label: 'Vendas', path: '/sales', icon: 'fas fa-hand-holding-dollar' },
    { label: 'Estoque', path: '/stock', icon: 'fas fa-paste' },
    { label: 'Configurações', path: '/settings', icon: 'fas fa-cog' }
  ]

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

  closeMenu() {
    this.isMenuOpen = false;
  }

   // Método para fazer logout
  logout() {
    // console.log('🚪 Realizando logout...');
    this.authService.logout();
    this.closeMenu(); // Fecha o menu mobile se estiver aberto
  }

  // Método para formatar o nome de exibição
  getDisplayName(): string {
    if (!this.currentUser) {
      this.loadUserData(); // Tenta carregar novamente se não tiver usuário
    }

    if (this.currentUser?.nomeCompleto) {
      return this.currentUser.nomeCompleto;
    }
    if (this.currentUser?.username) {
      // Capitaliza a primeira letra do username
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
}