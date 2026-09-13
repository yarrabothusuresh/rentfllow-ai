import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken', 'logout']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate'], { url: '/dashboard' });

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should attach Authorization Bearer token to /api/ requests when staff token exists', () => {
    authServiceSpy.getToken.and.returnValue('staff-jwt-token-xyz');

    httpClient.get('/api/bookings').subscribe();

    const req = httpMock.expectOne('/api/bookings');
    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe('Bearer staff-jwt-token-xyz');
    req.flush([]);
  });

  it('should not attach token to external URLs', () => {
    authServiceSpy.getToken.and.returnValue('staff-jwt-token-xyz');

    httpClient.get('https://external-api.com/data').subscribe();

    const req = httpMock.expectOne('https://external-api.com/data');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should logout and redirect on 401 response for staff APIs', () => {
    authServiceSpy.getToken.and.returnValue('expired-token');

    httpClient.get('/api/bookings').subscribe({
      next: () => fail('expected error'),
      error: (err) => {
        expect(err.status).toBe(401);
        expect(authServiceSpy.logout).toHaveBeenCalledWith('/login');
      }
    });

    const req = httpMock.expectOne('/api/bookings');
    req.flush({ error: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
  });
});
