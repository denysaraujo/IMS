import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    // ✅ Check if user has required role for route
    const requiredRoles = route.data?.['roles'] as string[] | undefined;
    if (requiredRoles?.length) {
      const user = authService.getCurrentUser();
      const userRole = user?.role?.toUpperCase();
      if (!user || !userRole || !requiredRoles.map(role => role.toUpperCase()).includes(userRole)) {
        router.navigate(['/dashboard']);
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
