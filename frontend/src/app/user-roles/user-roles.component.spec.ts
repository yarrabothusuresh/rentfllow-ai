import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { UserRolesComponent } from './user-roles.component';

describe('UserRolesComponent', () => {
  let component: UserRolesComponent;
  let fixture: ComponentFixture<UserRolesComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        UserRolesComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserRolesComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    const req = httpMock.expectOne('/api/roles/OWNER/permissions');
    req.flush(['DASHBOARD_VIEW', 'AI_COPILOT_USE']);
    expect(component).toBeTruthy();
  });

  it('should switch role and fetch permissions from backend', () => {
    // Initial fetch
    let req = httpMock.expectOne('/api/roles/OWNER/permissions');
    req.flush(['DASHBOARD_VIEW', 'AI_COPILOT_USE']);

    // Select SALES role
    component.selectRole('SALES');
    expect(component.selectedRoleCode).toBe('SALES');

    // Verify it triggers a call to /api/roles/SALES/permissions
    req = httpMock.expectOne('/api/roles/SALES/permissions');
    expect(req.request.method).toBe('GET');
    req.flush(['DASHBOARD_VIEW', 'LEAD_VIEW']);

    expect(component.backendPermissions).toEqual(['DASHBOARD_VIEW', 'LEAD_VIEW']);
  });
});
