import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken(); // ✅ Agora funciona sem parâmetros

  console.log('=== 🔐 AUTH INTERCEPTOR START ===');
  console.log('🔐 Interceptor - URL:', req.url);
  console.log('🔐 Interceptor - Method:', req.method);
  console.log('🔐 Interceptor - Token:', token ? 'Presente' : 'Ausente');
  
  // Se não for uma requisição para a API, não adicione o token
  if (!req.url.includes('/api/')) {
    console.log('🔐 Interceptor - Skipping (non-API request)');
    console.log('=== 🔐 AUTH INTERCEPTOR END ===');
    return next(req);
  }

  if (token) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    
    console.log('🔐 Interceptor - Headers com Authorization:', authReq.headers.get('Authorization')?.substring(0, 20) + '...');
    console.log('=== 🔐 AUTH INTERCEPTOR END ===');
    return next(authReq);
  }
  
  console.log('⚠️ Interceptor - No token, proceeding without Authorization header');
  console.log('=== 🔐 AUTH INTERCEPTOR END ===');
  return next(req);
};