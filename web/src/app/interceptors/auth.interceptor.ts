import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // Only add auth header for API requests
  if (!req.url.includes('/api/') && !req.url.includes(environment.apiUrl)) {
    return next(req);
  }

  const token = authService.getToken();
  
  let authReq = req;
  if (token) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // ✅ Auto logout on 401 response
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};