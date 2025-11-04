// user.service.ts - VERSÃO MELHORADA
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ✅ Move interface to separate file or keep if only used here
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

interface CreateUserRequest extends Omit<User, 'id'> {}
interface UpdateUserRequest extends Partial<Omit<User, 'id'>> {}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(user: CreateUserRequest): Observable<User> {
    const userToSend = this.sanitizeUserData(user);
    return this.http.post<User>(this.apiUrl, userToSend);
  }

  updateUser(id: number, user: UpdateUserRequest): Observable<User> {
    const userToUpdate = this.sanitizeUserData(user);
    
    // Remove empty password
    if (!userToUpdate.password) {
      delete userToUpdate.password;
    }
    
    return this.http.put<User>(`${this.apiUrl}/${id}`, userToUpdate);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  private sanitizeUserData(user: any): any {
    const { active, confirmPassword, ...sanitizedUser } = user;
    return sanitizedUser;
  }
}