import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Plan, SaasService, SignupRequest } from '../../services/saas.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.component.html'
})
export class SignupComponent implements OnInit {
  private readonly saasService = inject(SaasService);
  plans: Plan[] = [];
  loadingPlans = true;
  submitting = false;
  errorMessage = '';
  form: SignupRequest = {
    companyName: '',
    ownerName: '',
    companyDocument: '',
    email: '',
    username: '',
    password: '',
    planId: 0
  };

  ngOnInit(): void {
    this.saasService.getPublicPlans().subscribe({
      next: plans => {
        this.plans = plans;
        this.form.planId = plans[0]?.id ?? 0;
        this.loadingPlans = false;
      },
      error: () => {
        this.errorMessage = 'Não foi possível carregar os planos. Tente novamente mais tarde.';
        this.loadingPlans = false;
      }
    });
  }

  submit(): void {
    if (this.form.password.length < 8) {
      this.errorMessage = 'A senha deve ter pelo menos 8 caracteres.';
      return;
    }
    this.submitting = true;
    this.errorMessage = '';
    this.saasService.signup(this.form).subscribe({
      next: result => {
        if (result.checkoutUrl) {
          window.location.assign(result.checkoutUrl);
        } else {
          const trialEndsAt = encodeURIComponent(result.trialEndsAt ?? '');
          window.location.assign(`/login?trialEndsAt=${trialEndsAt}`);
        }
      },
      error: error => {
        this.errorMessage = error.error?.detail || error.error?.message
          || 'Não foi possível concluir o cadastro. Confira os dados e tente novamente.';
        this.submitting = false;
      }
    });
  }

  formatLimit(limit: number | null): string {
    return limit == null ? 'Ilimitado' : limit.toString();
  }
}
