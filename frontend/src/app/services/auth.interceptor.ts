import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Only intercept requests directed to our backend API endpoints
  if (!req.url.startsWith('/api/') && !req.url.startsWith('http://localhost:8080/api/')) {
    return next(req);
  }

  let token: string | null = null;
  const isPortalApi = req.url.includes('/api/portal/');
  const isAuthLoginApi = req.url.includes('/api/auth/login') || req.url.includes('/api/portal/auth/');

  if (isPortalApi) {
    try {
      const sessionStr = localStorage.getItem('rentflow_customer_session');
      if (sessionStr) {
        const session = JSON.parse(sessionStr);
        token = session.token || null;
      }
    } catch {
      token = null;
    }
  } else {
    token = authService.getToken();
  }

  let authReq = req;
  if (token && !isAuthLoginApi) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Prevent redirect loop if the 401 was from the login endpoint itself
        if (!isAuthLoginApi) {
          if (isPortalApi) {
            localStorage.removeItem('rentflow_customer_session');
            if (!router.url.startsWith('/portal/login')) {
              router.navigate(['/portal/login']);
            }
          } else {
            authService.logout('/login');
          }
        }
      } else if (error.status === 403) {
        console.warn('Access denied: You do not have permission to perform this action.');
      } else if (error.status === 429) {
        console.warn('Rate limit exceeded: Too many requests. Please wait and try again shortly.');
      }
      return throwError(() => error);
    })
  );
};
