import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  BillingEvent,
  CompanyAccount,
  Feature,
  Plan,
  SaasPayment,
  SaasService,
  Subscription
} from '../../services/saas.service';

type AdminTab = 'companies' | 'plans' | 'features' | 'subscriptions' | 'payments' | 'events';

@Component({
  selector: 'app-saas-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './saas-admin.component.html'
})
export class SaasAdminComponent implements OnInit {
  private readonly saasService = inject(SaasService);
  activeTab: AdminTab = 'companies';
  companies: CompanyAccount[] = [];
  plans: Plan[] = [];
  features: Feature[] = [];
  subscriptions: Subscription[] = [];
  billingEvents: BillingEvent[] = [];
  payments: SaasPayment[] = [];
  loading = true;
  saving = false;
  errorMessage = '';
  successMessage = '';
  planEditId: number | null = null;
  featureEditId: number | null = null;
  planForm = this.emptyPlan();
  featureForm = { code: '', name: '', description: '', active: true };
  readonly tabs: { id: AdminTab; label: string }[] = [
    { id: 'companies', label: 'Empresas' },
    { id: 'plans', label: 'Planos' },
    { id: 'features', label: 'Funcionalidades' },
    { id: 'subscriptions', label: 'Assinaturas' },
    { id: 'payments', label: 'Pagamentos' },
    { id: 'events', label: 'Eventos de cobrança' }
  ];

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.errorMessage = '';
    let completed = 0;
    const done = (): void => {
      completed += 1;
      if (completed === 6) this.loading = false;
    };
    this.saasService.getCompanies().subscribe({
      next: data => { this.companies = data; done(); },
      error: error => { this.fail(error); done(); }
    });
    this.saasService.getAdminPlans().subscribe({
      next: data => { this.plans = data; done(); },
      error: error => { this.fail(error); done(); }
    });
    this.saasService.getFeatures().subscribe({
      next: data => { this.features = data; done(); },
      error: error => { this.fail(error); done(); }
    });
    this.saasService.getSubscriptions().subscribe({
      next: data => { this.subscriptions = data; done(); },
      error: error => { this.fail(error); done(); }
    });
    this.saasService.getBillingEvents().subscribe({
      next: data => { this.billingEvents = data; done(); },
      error: error => { this.fail(error); done(); }
    });
    this.saasService.getAdminPayments().subscribe({
      next: data => { this.payments = data; done(); },
      error: error => { this.fail(error); done(); }
    });
  }

  selectTab(tab: AdminTab): void {
    this.activeTab = tab;
    this.errorMessage = '';
    this.successMessage = '';
  }

  editPlan(plan: Plan): void {
    this.planEditId = plan.id;
    this.planForm = {
      code: plan.code,
      name: plan.name,
      description: plan.description ?? '',
      amount: plan.amount,
      frequency: plan.frequency,
      frequencyType: plan.frequencyType,
      active: plan.active,
      maxUsers: plan.maxUsers,
      maxCustomers: plan.maxCustomers,
      maxInventoryItems: plan.maxInventoryItems,
      featureIds: plan.features.map(feature => feature.id)
    };
    this.activeTab = 'plans';
  }

  savePlan(): void {
    this.saving = true;
    this.errorMessage = '';
    const request = { ...this.planForm, id: this.planEditId ?? undefined };
    this.saasService.savePlan(request).subscribe({
      next: () => {
        this.successMessage = 'Plano salvo.';
        this.planEditId = null;
        this.planForm = this.emptyPlan();
        this.saving = false;
        this.loadAll();
      },
      error: error => this.fail(error)
    });
  }

  editFeature(feature: Feature): void {
    this.featureEditId = feature.id;
    this.featureForm = {
      code: feature.code,
      name: feature.name,
      description: feature.description ?? '',
      active: feature.active
    };
    this.activeTab = 'features';
  }

  saveFeature(): void {
    this.saving = true;
    this.errorMessage = '';
    this.saasService.saveFeature({ ...this.featureForm, id: this.featureEditId ?? undefined }).subscribe({
      next: () => {
        this.successMessage = 'Funcionalidade salva.';
        this.featureEditId = null;
        this.featureForm = { code: '', name: '', description: '', active: true };
        this.saving = false;
        this.loadAll();
      },
      error: error => this.fail(error)
    });
  }

  togglePlanFeature(id: number): void {
    this.planForm.featureIds = this.planForm.featureIds.includes(id)
      ? this.planForm.featureIds.filter(featureId => featureId !== id)
      : [...this.planForm.featureIds, id];
  }

  toggleCompany(company: CompanyAccount): void {
    this.saasService.setCompanyEnabled(company.id, !company.enabled).subscribe({
      next: updated => {
        company.enabled = updated.enabled;
        this.successMessage = updated.enabled ? 'Empresa reativada.' : 'Empresa suspensa.';
      },
      error: error => this.fail(error)
    });
  }

  featureNames(plan: Plan): string {
    return plan.features.map(feature => feature.name).join(', ');
  }

  formatLimit(limit: number | null): string {
    return limit == null ? 'Ilimitado' : limit.toString();
  }

  emptyPlan(): {
    code: string;
    name: string;
    description: string;
    amount: number;
    frequency: number;
    frequencyType: 'months' | 'days';
    active: boolean;
    maxUsers: number | null;
    maxCustomers: number | null;
    maxInventoryItems: number | null;
    featureIds: number[];
  } {
    return {
      code: '',
      name: '',
      description: '',
      amount: 0,
      frequency: 1,
      frequencyType: 'months',
      active: true,
      maxUsers: null,
      maxCustomers: null,
      maxInventoryItems: null,
      featureIds: []
    };
  }

  get enabledCompanyCount(): number {
    return this.companies.filter(company => company.enabled).length;
  }

  get activeSubscriptionCount(): number {
    return this.subscriptions.filter(subscription => subscription.status === 'ACTIVE').length;
  }

  get activeTrialCount(): number {
    const now = Date.now();
    return this.subscriptions.filter(subscription =>
      subscription.status === 'TRIALING'
      && subscription.trialEndsAt != null
      && new Date(subscription.trialEndsAt).getTime() > now
    ).length;
  }

  get approvedPaymentTotal(): number {
    return this.payments
      .filter(payment => payment.status.toLowerCase() === 'approved')
      .reduce((total, payment) => total + payment.amount, 0);
  }

  private fail(error: { error?: { detail?: string; message?: string } }): void {
    this.errorMessage = error.error?.detail || error.error?.message || 'Não foi possível concluir a operação.';
    this.saving = false;
  }
}
