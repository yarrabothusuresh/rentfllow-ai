import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { ProfileService } from '../services/profile.service';
import { of } from 'rxjs';
import { INITIAL_BUSINESS_PROFILE } from '../data/business-profile.data';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockProfileService: any;

  beforeEach(async () => {
    mockProfileService = {
      getProfile: () => of(INITIAL_BUSINESS_PROFILE)
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, RouterTestingModule, HttpClientTestingModule],
      providers: [
        { provide: ProfileService, useValue: mockProfileService }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
