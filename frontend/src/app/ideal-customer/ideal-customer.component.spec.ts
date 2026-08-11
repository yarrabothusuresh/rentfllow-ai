import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { IdealCustomerComponent } from './ideal-customer.component';

describe('IdealCustomerComponent', () => {
  let component: IdealCustomerComponent;
  let fixture: ComponentFixture<IdealCustomerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        IdealCustomerComponent,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IdealCustomerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate score reactively as options are selected', () => {
    expect(component.quizScore).toBe(0);
    expect(component.isQuizFullyAnswered).toBeFalse();

    // Select Q1 option 2 (score 25)
    component.selectOption(0, 2);
    expect(component.quizScore).toBe(25);

    // Select Q2 option 2 (score 25)
    component.selectOption(1, 2);
    expect(component.quizScore).toBe(50);

    // Select Q3 option 2 (score 25)
    component.selectOption(2, 2);
    expect(component.quizScore).toBe(75);

    // Select Q4 option 0 (score 25)
    component.selectOption(3, 0);
    expect(component.quizScore).toBe(100);
    expect(component.isQuizFullyAnswered).toBeTrue();
    expect(component.scoreCategory).toBe('Ideal RentFlow AI customer');
  });
});
