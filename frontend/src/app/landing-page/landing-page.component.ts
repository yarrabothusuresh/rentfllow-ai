import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CUSTOMER_PERSONAS } from '../data/personas.data';
import { VALUE_PROPOSITIONS } from '../data/value-propositions.data';
import { CustomerPersona } from '../models/customer-persona.model';
import { ValueProposition } from '../models/value-proposition.model';

interface SolutionStep {
  name: string;
  description: string;
}

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './landing-page.component.html',
  styleUrl: './landing-page.component.scss'
})
export class LandingPageComponent implements OnInit, OnDestroy {
  personas: CustomerPersona[] = CUSTOMER_PERSONAS;
  valueProps: ValueProposition[] = VALUE_PROPOSITIONS;

  // Solution Stepper
  solutionSteps: SolutionStep[] = [
    { name: 'Customer', description: 'Initiates inquiry via chat, email, or web form.' },
    { name: 'AI Sales Agent', description: 'Understands customer requirements and identifies the right rental products.' },
    { name: 'Availability', description: 'Checks date-specific inventory before recommending products.' },
    { name: 'Quote', description: 'Calculates rental, delivery, setup, tax and estimated margin.' },
    { name: 'Payment', description: 'Secures booking with automated digital deposit or full payment.' },
    { name: 'Booking', description: 'Instantly confirms event details and generates contract.' },
    { name: 'Inventory', description: 'Allocates items automatically to prevent double-booking.' },
    { name: 'Warehouse', description: 'Creates real-time loading/pickup lists for the team.' },
    { name: 'Delivery', description: 'Optimizes dispatch and routing for delivery drivers.' },
    { name: 'Profit', description: 'Shows expected profitability before the business accepts the booking.' }
  ];
  activeSolutionStepIndex = 1; // Default to AI Sales Agent

  // Before vs After Workflow Simulation
  beforeSteps = [
    'Customer inquiry',
    'Employee checks email',
    'Search inventory',
    'Check pricing',
    'Create quote',
    'Send email',
    'Wait',
    'Follow up',
    'Create booking'
  ];
  afterSteps = [
    'Customer inquiry',
    'AI understands request',
    'AI checks availability',
    'AI recommends package',
    'AI calculates quote',
    'Customer receives quote',
    'Customer accepts',
    'Booking created'
  ];

  activeBeforeStep = -1;
  activeAfterStep = -1;
  beforeIntervalId: any;
  afterIntervalId: any;

  // ROI Calculator inputs (with defaults)
  roiInquiries = 300;
  roiMinutes = 30;
  roiHourlyCost = 30;

  // Target customer cards
  targetCategories = [
    {
      title: 'Wedding Rentals',
      icon: '💍',
      description: 'High-end aesthetic furniture, custom arches, centerpieces, and premium tableware.',
      products: 'Farm tables, cross-back chairs, drapery, custom backdrops'
    },
    {
      title: 'Party Rentals',
      icon: '🎈',
      description: 'Standard event setups, fun food machines, stages, and essential party gear.',
      products: 'Folding chairs, plastic tables, popcorn machines, basic staging'
    },
    {
      title: 'Tent Rentals',
      icon: '⛺',
      description: 'Heavy-duty structural tents, professional sizing calculations, and anchoring systems.',
      products: 'Frame tents, pole tents, sidewalls, tent heaters, lighting'
    },
    {
      title: 'Event Furniture',
      icon: '🛋️',
      description: 'Luxury lounge seating, portable bar configurations, and themed staging setups.',
      products: 'Chesterfield sofas, LED bars, bar stools, lounge chairs'
    },
    {
      title: 'Event Décor',
      icon: '🕯️',
      description: 'Fine details including lighting backdrops, tabletop accessories, and room decorations.',
      products: 'Candelabras, floral walls, table runners, neon signs'
    },
    {
      title: 'AV & Lighting',
      icon: '🔊',
      description: 'Professional grade sound systems, dynamic uplighting, and video projector kits.',
      products: 'PA speakers, wireless mics, LED uplights, projector screens'
    }
  ];

  ngOnInit() {
    this.startAfterSimulation();
  }

  ngOnDestroy() {
    this.stopBeforeSimulation();
    this.stopAfterSimulation();
  }

  selectSolutionStep(index: number) {
    this.activeSolutionStepIndex = index;
  }

  // Before Workflow Simulation
  playBeforeSimulation() {
    this.stopBeforeSimulation();
    this.activeBeforeStep = 0;
    this.beforeIntervalId = setInterval(() => {
      if (this.activeBeforeStep < this.beforeSteps.length - 1) {
        this.activeBeforeStep++;
      } else {
        this.activeBeforeStep = 0;
      }
    }, 1200);
  }

  stopBeforeSimulation() {
    if (this.beforeIntervalId) {
      clearInterval(this.beforeIntervalId);
    }
    this.activeBeforeStep = -1;
  }

  // After Workflow Simulation
  playAfterSimulation() {
    this.stopAfterSimulation();
    this.activeAfterStep = 0;
    this.afterIntervalId = setInterval(() => {
      if (this.activeAfterStep < this.afterSteps.length - 1) {
        this.activeAfterStep++;
      } else {
        this.activeAfterStep = 0;
      }
    }, 1000);
  }

  stopAfterSimulation() {
    if (this.afterIntervalId) {
      clearInterval(this.afterIntervalId);
    }
    this.activeAfterStep = -1;
  }

  private startAfterSimulation() {
    // Start after-workflow simulation by default to wow the user
    this.playAfterSimulation();
  }

  // ROI Computations
  get currentLaborHours(): number {
    return Math.round((this.roiInquiries * this.roiMinutes) / 60);
  }

  get currentLaborCost(): number {
    return this.currentLaborHours * this.roiHourlyCost;
  }

  get potentialHoursSaved(): number {
    return Math.round(this.currentLaborHours * 0.80); // 80% saved
  }

  get potentialSavings(): number {
    return this.potentialHoursSaved * this.roiHourlyCost;
  }

  scrollTo(elementId: string): void {
    const element = document.getElementById(elementId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
