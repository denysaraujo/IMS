import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id?: number;
  nomeCompleto: string;
  username: string;
  password?: string;
  role: string;
  email?: string;
  telefone?: string;
  celular?: string;
  enabled?: boolean;
  accountNonLocked?: boolean;
  accountNonExpired?: boolean;
  credentialsNonExpired?: boolean;
  loginAttempts?: number;
  lastLogin?: string;
  createdAt?: string;
  updatedAt?: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  getUsers(): Observable<User[]> {
    console.log('🔍 [USER SERVICE] Buscando usuários...');
    return this.http.get<User[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: User): Observable<User> {
    const userToSend = { ...user };
    delete (userToSend as any).active;
    delete (userToSend as any).confirmPassword;
    
    console.log('📤 [USER SERVICE] Criando usuário:', userToSend);
    return this.http.post<User>(this.apiUrl, userToSend);
  }

  updateUser(id: number, user: User): Observable<User> {
    const userToUpdate = { ...user };
    delete (userToUpdate as any).active;
    delete (userToUpdate as any).confirmPassword;
    
    // Remove password se estiver vazio
    if (!userToUpdate.password || userToUpdate.password.trim() === '') {
      delete userToUpdate.password;
    }
    
    console.log('📤 [USER SERVICE] Atualizando usuário ID:', id);
    console.log('📦 [USER SERVICE] Dados:', userToUpdate);
    
    return this.http.put<User>(`${this.apiUrl}/${id}`, userToUpdate);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}