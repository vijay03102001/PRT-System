import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, AuthResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'any'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:4200/api/auth';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  authService: any;

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken(); // Get token from storage
    if (!token) {
      console.error("No token found!");
      return new HttpHeaders();
    }

    return new HttpHeaders({
      Authorization: `Bearer ${token}`, 
      'Content-Type': 'application/json'
    });
  }

  constructor(private http: HttpClient) {
    this.checkInitialAuth();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/authenticate`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          this.isAuthenticatedSubject.next(true);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.isAuthenticatedSubject.next(false);
  }

  isAuthenticated(): Observable<boolean> {
    return this.isAuthenticatedSubject.asObservable();
  }

  private checkInitialAuth(): void {
    const token = localStorage.getItem('token');
    this.isAuthenticatedSubject.next(!!token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }





}
