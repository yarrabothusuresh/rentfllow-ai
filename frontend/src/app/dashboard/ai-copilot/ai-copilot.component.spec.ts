import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiCopilotComponent } from './ai-copilot.component';

describe('AiCopilotComponent', () => {
  let component: AiCopilotComponent;
  let fixture: ComponentFixture<AiCopilotComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiCopilotComponent]
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
