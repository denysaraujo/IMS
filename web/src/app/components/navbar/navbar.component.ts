import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  imports: [CommonModule],
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  isDarkMode: boolean = false; // Control dark mode state
  isMenuOpen: boolean = false; // Control mobile menu collapse

  // Toggle Dark Mode
  toggleDarkMode() {
    this.isDarkMode = !this.isDarkMode;
    const htmlElement = document.documentElement;
    if (this.isDarkMode) {
      htmlElement.classList.add('dark');
    } else {
      htmlElement.classList.remove('dark');
    }
  }

  // Toggle Mobile Menu
  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }
}