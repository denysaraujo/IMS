import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginData = { username: '', password: '' };
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.loginData.username || !this.loginData.password) {
      this.errorMessage = 'Por favor, preencha todos os campos';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginData.username, this.loginData.password)
      .subscribe({
        next: (response) => {
          this.router.navigate(['/dashboard']);
          this.loading = false;
        },
        error: (error) => {
          console.error('Login error:', error);
          this.errorMessage = this.getErrorMessage(error);
          this.loading = false;
        }
      });
  }

  private getErrorMessage(error: any): string {
    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique se a API está rodando.';
    } else if (error.status === 401) {
      return 'Usuário ou senha inválidos';
    } else if (error.status === 500) {
      return 'Erro interno do servidor. Tente novamente.';
    } else {
      return 'Erro ao conectar ao servidor';
    }
  }

  fillTestCredentials() {
    this.loginData.username = 'admin';
    this.loginData.password = 'admin123';
  }
}