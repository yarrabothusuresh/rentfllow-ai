import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { AiCopilotComponent } from './ai-copilot.component';

describe('AiCopilotComponent', () => {
  let component: AiCopilotComponent;
  let fixture: ComponentFixture<AiCopilotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiCopilotComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AiCopilotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
