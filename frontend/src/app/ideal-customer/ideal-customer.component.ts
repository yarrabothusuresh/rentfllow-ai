import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { IcpProfile } from '../models/icp-profile.model';
import { IcpPainPoint } from '../models/icp-pain-point.model';
import { ICP_PAIN_POINTS, ICP_COMPARISONS } from '../data/icp.data';

interface Persona {
  name: string;
  role: string;
  avatar: string;
  goals: string[];
}

@Component({
  selector: 'app-ideal-customer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ideal-customer.component.html',
  styleUrl: './ideal-customer.component.scss'
})
export class IdealCustomerComponent implements OnInit {
  icpProfile!: IcpProfile;
  painPoints: IcpPainPoint[] = ICP_PAIN_POINTS;
  comparisons = ICP_COMPARISONS;
  isLoading = true;

  // Primary customer categories
  categories = [
    { title: 'Event Rental Companies', desc: 'Focusing on weddings, corporate events, and galas.' },
    { title: 'Party Rental Companies', desc: 'Providing tents, tables, chairs, and bouncy castles.' },
    { title: 'Wedding Rental Companies', desc: 'High-end aesthetic designs, decor, arches, and boutique tables.' },
    { title: 'Tent & Event Equipment Companies', desc: 'Heavy-duty structural setups and large equipment logistics.' },
    { title: 'Event Furniture Companies', desc: 'Lounge settings, bars, staging, and seating design layouts.' }
  ];

  // Customer personas for ICP
  personas: Persona[] = [
    {
      name: 'Mike',
      role: 'Business Owner',
      avatar: '👨‍💼',
      goals: ['Increase revenue & profit margins', 'Reduce operational overhead', 'Real-time visibility into business performance']
    },
    {
      name: 'Sarah',
      role: 'Sales Manager',
      avatar: '👩‍💼',
      goals: ['Respond faster to inquiries', 'Convert more inquiries into paid bookings', 'Automate quotes and follow-ups']
    },
    {
      name: 'James',
      role: 'Warehouse Manager',
      avatar: '👨‍🏭',
      goals: ['Accurate item picking lists', 'Avoid double-bookings and shortages', 'Reduce operational delivery errors']
    },
    {
      name: 'Alex',
      role: 'Operations Manager',
      avatar: '⚙️',
      goals: ['Coordinate seamless deliveries', 'Optimize route scheduling for drivers', 'Avoid logistics scheduling conflicts']
    }
  ];

  // Interactive Quiz state
  selectedAnswers: { [key: number]: number } = {
    0: -1,
    1: -1,
    2: -1,
    3: -1
  };

  quizQuestions = [
    {
      text: 'How many rental products do you manage?',
      options: [
        { text: 'Less than 100', score: 10 },
        { text: '100 – 500', score: 15 },
        { text: '500 – 5,000', score: 25 },
        { text: '5,000+', score: 25 }
      ]
    },
    {
      text: 'How many events do you manage per month?',
      options: [
        { text: 'Less than 20', score: 10 },
        { text: '20 – 50', score: 15 },
        { text: '50 – 200', score: 25 },
        { text: '200+', score: 25 }
      ]
    },
    {
      text: 'How many employees?',
      options: [
        { text: '1 – 5', score: 10 },
        { text: '6 – 10', score: 15 },
        { text: '11 – 50', score: 25 },
        { text: '50+', score: 25 }
      ]
    },
    {
      text: 'How do you currently create quotes?',
      options: [
        { text: 'Spreadsheet', score: 25 },
        { text: 'Email/manual', score: 25 },
        { text: 'Rental software', score: 20 },
        { text: 'Custom system', score: 15 }
      ]
    }
  ];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchIcpProfile();
  }

  fetchIcpProfile() {
    this.http.get<IcpProfile>('/api/icp/profile').pipe(
      catchError(error => {
        console.warn('Backend ICP profile API failed, falling back to mock data', error);
        return of({
          name: 'US Event & Party Rental Company',
          revenueRange: '$500K - $10M',
          employeeRange: '10 - 50',
          warehouseRange: '1 - 3',
          productRange: '500 - 10,000+',
          market: 'United States',
          validationStatus: 'INITIAL_ASSUMPTION'
        } as IcpProfile);
      })
    ).subscribe(data => {
      this.icpProfile = data;
      this.isLoading = false;
    });
  }

  selectOption(qIndex: number, oIndex: number) {
    this.selectedAnswers[qIndex] = oIndex;
  }

  get quizScore(): number {
    let score = 0;
    for (let i = 0; i < this.quizQuestions.length; i++) {
      const selectedOptIdx = this.selectedAnswers[i];
      if (selectedOptIdx !== -1) {
        score += this.quizQuestions[i].options[selectedOptIdx].score;
      }
    }
    return score;
  }

  get isQuizFullyAnswered(): boolean {
    return Object.values(this.selectedAnswers).every(idx => idx !== -1);
  }

  get scoreCategory(): string {
    const score = this.quizScore;
    if (score <= 30) return 'Early-stage rental business';
    if (score <= 60) return 'Growing rental business';
    if (score <= 80) return 'Strong RentFlow AI candidate';
    return 'Ideal RentFlow AI customer';
  }
}
