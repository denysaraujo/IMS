import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { UsersComponent } from './components/users/users.component';
import { InventoryComponent } from './components/inventory/inventory.component';
import { SalesComponent } from './components/sales/sales.component';
import { ReportsComponent } from './components/reports/reports.component';
import { CustomersComponent } from './components/customers/customers.component';
import { RentalComponent } from './components/rental/rental.component';
import { CompanyComponent } from './components/company/company.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'users',
    component: UsersComponent,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: 'company',
    component: CompanyComponent,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'inventory',
    component: InventoryComponent,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'MANAGER', 'SUPERVISOR'] }
  },
  {
    path: 'sales',
    component: SalesComponent,
    canActivate: [authGuard]
  },
  {
    path: 'reports',
    component: ReportsComponent,
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'MANAGER'] }
  },
  {
    path: 'customers',
    component: CustomersComponent,
    canActivate: [authGuard]
  },
  {
    path: 'rentals',
    component: RentalComponent,
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/dashboard' },
];
