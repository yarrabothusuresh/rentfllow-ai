import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService, AuthResponse } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    sessionStorage.clear();
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should be created and initial state unauthenticated', () => {
    expect(service).toBeTruthy();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
  });

  it('should store token and user on successful login', () => {
    const mockResponse: AuthResponse = {
      accessToken: 'test-jwt-token-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      user: {
        id: '11111111-1111-1111-1111-111111111111',
        email: 'owner@demo.local',
        name: 'John Anderson',
        role: 'OWNER',
        tenantId: '99999999-9999-9999-9999-999999999999'
      }
    };

    service.login({ email: 'owner@demo.local', password: 'password' }).subscribe(res => {
      expect(res.accessToken).toBe('test-jwt-token-123');
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.getToken()).toBe('test-jwt-token-123');
      expect(service.hasRole('OWNER')).toBeTrue();
      expect(service.hasRole('WAREHOUSE')).toBeFalse();
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should clear storage and navigate on logout', () => {
    sessionStorage.setItem('rentflow_auth_token', 'token');
    sessionStorage.setItem('rentflow_auth_user', JSON.stringify({ email: 'test@example.com', role: 'ADMIN' }));

    service.logout('/login');

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
