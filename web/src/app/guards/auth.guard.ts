import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    // ✅ Check if user has required role for route
    const requiredRole = route.data?.['role'];
    if (requiredRole) {
      const user = authService.getCurrentUser();
      if (user?.role !== requiredRole) {
        router.navigate(['/unauthorized']);
        return false;
      }
    }
    return true;
  } else {
    // ✅ Preserve attempted URL for redirect after login
    router.navigate(['/login'], { 
      queryParams: { returnUrl: state.url } 
    });
    return false;
  }
};