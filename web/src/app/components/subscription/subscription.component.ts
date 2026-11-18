import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Plan, SaasPayment, SaasService, Subscription } from '../../services/saas.service';

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subscription.component.html'
})
export class SubscriptionComponent implements OnInit {
  private readonly saasService = inject(SaasService);
  subscription: Subscription | null = null;
  plans: Plan[] = [];
  payments: SaasPayment[] = [];
  selectedPlanId = 0;
  loading = true;
  busy = false;
  message = '';
  errorMessage = '';

  ngOnInit(): void {
    this.load();
    this.saasService.getMyPayments().subscribe({
      next: payments => this.payments = payments,
      error: () => this.errorMessage = 'Não foi possível carregar o histórico de pagamentos.'
    });
  }

  load(): void {
    this.loading = true;
    this.saasService.getMySubscription().subscribe({
      next: subscription => {
        this.subscription = subscription;
        this.loading = false;
        if (!subscription || subscription.status !== 'ACTIVE') this.loadPlans();
      },
      error: error => {
        this.loading = false;
        this.errorMessage = error.error?.detail || error.error?.message || 'Não foi possível carregar a assinatura.';
      }
    });
  }

  subscribe(): void {
    if (!this.selectedPlanId) return;
    this.busy = true;
    this.errorMessage = '';
    this.saasService.subscribe(this.selectedPlanId).subscribe({
      next: result => window.location.assign(result.checkoutUrl),
      error: error => {
        this.busy = false;
        this.errorMessage = error.error?.detail || error.error?.message || 'Não foi possível iniciar o checkout.';
      }
    });
  }

  cancel(): void {
    if (!confirm('Confirma o cancelamento da assinatura?')) return;
    this.busy = true;
    this.errorMessage = '';
    this.saasService.cancelSubscription().subscribe({
      next: subscription => {
        this.subscription = subscription;
        this.busy = false;
        this.message = 'Assinatura cancelada.';
        this.loadPlans();
      },
      error: error => {
        this.busy = false;
        this.errorMessage = error.error?.detail || error.error?.message || 'Não foi possível cancelar a assinatura.';
      }
    });
  }

  private loadPlans(): void {
    this.saasService.getPublicPlans().subscribe({
      next: plans => {
        this.plans = plans;
        this.selectedPlanId = plans[0]?.id ?? 0;
      },
      error: () => this.errorMessage = 'Não foi possível carregar os planos disponíveis.'
    });
  }
}
