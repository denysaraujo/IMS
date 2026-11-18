import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Feature {
  id: number;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface Plan {
  id: number;
  code: string;
  name: string;
  description: string | null;
  amount: number;
  currency: string;
  frequency: number;
  frequencyType: 'months' | 'days';
  active: boolean;
  maxUsers: number | null;
  maxCustomers: number | null;
  maxInventoryItems: number | null;
  features: Feature[];
}

export interface CompanyAccount {
  id: number;
  name: string;
  document: string | null;
  email: string | null;
  enabled: boolean;
  createdAt: string;
  subscriptionStatus: string | null;
  planName: string | null;
  userCount: number;
  customerCount: number;
  inventoryItemCount: number;
  saleCount: number;
}

export interface Subscription {
  id: number;
  companyId: number;
  companyName: string | null;
  planId: number;
  planName: string;
  amount: number;
  currency: string;
  frequency: number;
  frequencyType: string;
  status: string;
  provider: string;
  providerSubscriptionId: string | null;
  checkoutUrl: string | null;
  createdAt: string;
  trialEndsAt: string | null;
}

export interface BillingEvent {
  id: number;
  providerEventId: string;
  providerSubscriptionId: string | null;
  type: string;
  status: string;
  receivedAt: string;
}

export interface SaasPayment {
  id: number;
  companyId: number;
  companyName: string;
  subscriptionId: number;
  planName: string;
  providerPaymentId: string;
  amount: number;
  currency: string;
  status: string;
  paidAt: string | null;
  createdAt: string;
}

export interface SignupRequest {
  companyName: string;
  ownerName: string;
  companyDocument: string;
  email: string;
  username: string;
  password: string;
  planId: number;
}

@Injectable({ providedIn: 'root' })
export class SaasService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getPublicPlans(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${this.apiUrl}/saas/public/plans`);
  }

  signup(request: SignupRequest): Observable<{ checkoutUrl: string | null; status: string; trialEndsAt: string | null }> {
    return this.http.post<{ checkoutUrl: string | null; status: string; trialEndsAt: string | null }>(`${this.apiUrl}/saas/public/signup`, request);
  }

  getAdminPlans(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${this.apiUrl}/saas/admin/plans`);
  }

  savePlan(plan: Omit<Plan, 'id' | 'currency' | 'features'> & { id?: number; featureIds: number[] }): Observable<Plan> {
    const body = {
      code: plan.code,
      name: plan.name,
      description: plan.description,
      amount: plan.amount,
      frequency: plan.frequency,
      frequencyType: plan.frequencyType,
      active: plan.active,
      maxUsers: plan.maxUsers,
      maxCustomers: plan.maxCustomers,
      maxInventoryItems: plan.maxInventoryItems,
      featureIds: plan.featureIds
    };
    return plan.id
      ? this.http.put<Plan>(`${this.apiUrl}/saas/admin/plans/${plan.id}`, body)
      : this.http.post<Plan>(`${this.apiUrl}/saas/admin/plans`, body);
  }

  getFeatures(): Observable<Feature[]> {
    return this.http.get<Feature[]>(`${this.apiUrl}/saas/admin/features`);
  }

  saveFeature(feature: Omit<Feature, 'id'> & { id?: number }): Observable<Feature> {
    const body = {
      code: feature.code,
      name: feature.name,
      description: feature.description,
      active: feature.active
    };
    return feature.id
      ? this.http.put<Feature>(`${this.apiUrl}/saas/admin/features/${feature.id}`, body)
      : this.http.post<Feature>(`${this.apiUrl}/saas/admin/features`, body);
  }

  getCompanies(): Observable<CompanyAccount[]> {
    return this.http.get<CompanyAccount[]>(`${this.apiUrl}/saas/admin/companies`);
  }

  setCompanyEnabled(id: number, enabled: boolean): Observable<CompanyAccount> {
    return this.http.put<CompanyAccount>(`${this.apiUrl}/saas/admin/companies/${id}/enabled`, { enabled });
  }

  getSubscriptions(): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.apiUrl}/saas/admin/subscriptions`);
  }

  getBillingEvents(): Observable<BillingEvent[]> {
    return this.http.get<BillingEvent[]>(`${this.apiUrl}/saas/admin/billing-events`);
  }

  getAdminPayments(): Observable<SaasPayment[]> {
    return this.http.get<SaasPayment[]>(`${this.apiUrl}/saas/admin/payments`);
  }

  getMySubscription(): Observable<Subscription | null> {
    return this.http.get<Subscription | null>(`${this.apiUrl}/billing/me`);
  }

  getMyPayments(): Observable<SaasPayment[]> {
    return this.http.get<SaasPayment[]>(`${this.apiUrl}/billing/payments`);
  }

  subscribe(planId: number): Observable<{ checkoutUrl: string }> {
    return this.http.post<{ checkoutUrl: string }>(`${this.apiUrl}/billing/subscribe`, { planId });
  }

  cancelSubscription(): Observable<Subscription> {
    return this.http.post<Subscription>(`${this.apiUrl}/billing/cancel`, {});
  }
}
