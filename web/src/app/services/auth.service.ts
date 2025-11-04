import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  username: string;
  nomeCompleto: string;
  role: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private apiUrl = environment.apiUrl;
  
  // ✅ Add reactive state management
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.initializeAuthState();
  }

  private initializeAuthState(): void {
    if (isPlatformBrowser(this.platformId)) {
      const user = this.getCurrentUser();
      this.currentUserSubject.next(user);
    }
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, { 
      username, 
      password 
    }).pipe(
      tap(response => {
        this.setSession(response.token, response.user);
        this.currentUserSubject.next(response.user);
      })
    );
  }

  private setSession(token: string, user: User): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('auth_token', token);
      localStorage.setItem('current_user', JSON.stringify(user));
      
      // ✅ Set token expiration (assuming JWT with 1 hour expiry)
      const expiresAt = new Date();
      expiresAt.setHours(expiresAt.getHours() + 1);
      localStorage.setItem('token_expires_at', expiresAt.toISOString());
    }
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('current_user');
      localStorage.removeItem('token_expires_at');
    }
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    if (!isPlatformBrowser(this.platformId)) return false;
    
    const token = this.getToken();
    const expiresAt = localStorage.getItem('token_expires_at');
    
    if (!token || !expiresAt) return false;
    
    // ✅ Check token expiration
    return new Date() < new Date(expiresAt);
  }

  getCurrentUser(): User | null {
    if (isPlatformBrowser(this.platformId)) {
      try {
        const user = localStorage.getItem('current_user');
        return user ? JSON.parse(user) : null;
      } catch {
        this.logout(); // ✅ Clear invalid data
        return null;
      }
    }
    return null;
  }

  getToken(): string | null { 
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('auth_token');
    }
    return null;
  }
}