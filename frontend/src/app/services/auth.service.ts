import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface UserSummary {
  id: string;
  email: string;
  name: string;
  role: string;
  tenantId: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'rentflow_auth_token';
  private readonly USER_KEY = 'rentflow_auth_user';

  private currentUserSubject = new BehaviorSubject<UserSummary | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  private getStoredUser(): UserSummary | null {
    try {
      const userJson = sessionStorage.getItem(this.USER_KEY);
      return userJson ? JSON.parse(userJson) : null;
    } catch {
      return null;
    }
  }

  private hasValidToken(): boolean {
    return !!sessionStorage.getItem(this.TOKEN_KEY);
  }

  public getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  public getCurrentUser(): UserSummary | null {
    return this.currentUserSubject.value;
  }

  public isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  public getRole(): string {
    const user = this.getCurrentUser();
    return user?.role || 'ANONYMOUS';
  }

  public hasRole(role: string): boolean {
    return this.getRole().toUpperCase() === role.toUpperCase();
  }

  public hasAnyRole(roles: string[]): boolean {
    const current = this.getRole().toUpperCase();
    return roles.map(r => r.toUpperCase()).includes(current);
  }

  public login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        sessionStorage.setItem(this.TOKEN_KEY, response.accessToken);
        sessionStorage.setItem(this.USER_KEY, JSON.stringify(response.user));
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  public logout(redirectUrl: string = '/login'): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate([redirectUrl]);
  }
}
