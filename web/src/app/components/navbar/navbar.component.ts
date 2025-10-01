import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';

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

  constructor(@Inject(PLATFORM_ID) private platformId: any) {}

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

  ngOnInit() {
    // Verificar tema salvo apenas no cliente
    if (isPlatformBrowser(this.platformId)) {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme === 'dark') {
        this.isDarkMode = true;
        document.documentElement.classList.add('dark');
      }
    }
  }
}