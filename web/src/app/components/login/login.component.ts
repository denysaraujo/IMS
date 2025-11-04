import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  loginData = { username: '', password: '' };
  loading = false;
  errorMessage = '';
  returnUrl = '/dashboard';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // ✅ Get return url from route parameters or default to '/dashboard'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
    
    // ✅ Redirect if already logged in
    if (this.authService.isLoggedIn()) {
      this.router.navigate([this.returnUrl]);
    }
  }

  onSubmit() {
    if (!this.isFormValid()) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginData.username, this.loginData.password)
      .subscribe({
        next: () => {
          this.router.navigateByUrl(this.returnUrl);
        },
        error: (error) => {
          this.handleError(error);
          this.loading = false;
        }
      });
  }

  private isFormValid(): boolean {
    if (!this.loginData.username.trim() || !this.loginData.password.trim()) {
      this.errorMessage = 'Por favor, preencha todos os campos';
      return false;
    }
    
    if (this.loginData.username.length < 3) {
      this.errorMessage = 'Usuário deve ter pelo menos 3 caracteres';
      return false;
    }
    
    return true;
  }

  isDarkMode = false;

  toggleTheme() {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  private handleError(error: any): void {
    console.error('Login error:', error);
    
    if (error.status === 0) {
      this.errorMessage = 'Servidor indisponível. Tente novamente em alguns instantes.';
    } else if (error.status === 401) {
      this.errorMessage = 'Credenciais inválidas. Verifique seu usuário e senha.';
    } else if (error.status >= 500) {
      this.errorMessage = 'Erro interno do servidor. Nossa equipe foi notificada.';
    } else {
      this.errorMessage = 'Erro inesperado. Tente novamente.';
    }
  }
}