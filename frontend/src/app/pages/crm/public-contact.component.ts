import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CrmService } from '../../services/crm.service';
import { PublicInquiryRequest } from '../../models/crm.models';

@Component({
  selector: 'app-public-contact',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="public-contact-wrapper">
      <div class="contact-card">
        <header class="contact-header">
          <div class="store-badge">
            <i class="bi bi-calendar2-heart-fill"></i> Rental Inquiries
          </div>
          <h1>Plan Your Next Event With Us</h1>
          <p class="subtitle">Tell us about your event dates, guest count, and equipment wishlist. Our event rental specialists will prepare a custom proposal.</p>
        </header>

        <!-- Success Message -->
        <div *ngIf="submitted" class="success-banner">
          <i class="bi bi-check-circle-fill"></i>
          <h2>Inquiry Received!</h2>
          <p>{{ successMessage }}</p>
          <button (click)="resetForm()" class="btn btn-secondary">Submit Another Inquiry</button>
        </div>

        <!-- Contact Form -->
        <form *ngIf="!submitted" (ngSubmit)="onSubmit()" class="inquiry-form">
          <!-- Honeypot anti-spam (hidden) -->
          <input type="text" [(ngModel)]="inquiry.honeypot" name="hp_field" style="display:none;" />

          <div class="form-row">
            <div class="form-group col">
              <label>Your Name *</label>
              <input type="text" [(ngModel)]="inquiry.name" name="name" required placeholder="Full Name" />
            </div>
            <div class="form-group col">
              <label>Company / Organization</label>
              <input type="text" [(ngModel)]="inquiry.company" name="company" placeholder="Company LLC (optional)" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group col">
              <label>Email Address *</label>
              <input type="email" [(ngModel)]="inquiry.email" name="email" required placeholder="your.email@example.com" />
            </div>
            <div class="form-group col">
              <label>Phone Number</label>
              <input type="tel" [(ngModel)]="inquiry.phone" name="phone" placeholder="(555) 000-0000" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group col">
              <label>Event Type</label>
              <select [(ngModel)]="inquiry.eventType" name="eventType">
                <option value="WEDDING">Wedding</option>
                <option value="CORPORATE">Corporate Event / Gala</option>
                <option value="CONFERENCE">Conference / Trade Show</option>
                <option value="BIRTHDAY">Birthday / Anniversary</option>
                <option value="FESTIVAL">Festival / Concert</option>
                <option value="GRADUATION">Graduation</option>
                <option value="PRIVATE_PARTY">Private Party</option>
                <option value="OTHER">Other Occasion</option>
              </select>
            </div>
            <div class="form-group col">
              <label>Event Date</label>
              <input type="date" [(ngModel)]="inquiry.eventDate" name="eventDate" />
            </div>
          </div>

          <div class="form-group">
            <label>Rental Details & Equipment Wishlist *</label>
            <textarea
              [(ngModel)]="inquiry.message"
              name="message"
              required
              rows="4"
              placeholder="Describe your event needs (e.g., number of tables, chairs, tent sizes, staging, delivery location, setup requirements)..."
            ></textarea>
          </div>

          <div *ngIf="errorMessage" class="error-msg">
            <i class="bi bi-exclamation-circle"></i> {{ errorMessage }}
          </div>

          <button type="submit" class="btn btn-primary btn-submit" [disabled]="submitting || !inquiry.name || !inquiry.email || !inquiry.message">
            <span *ngIf="!submitting">Submit Rental Inquiry &rarr;</span>
            <span *ngIf="submitting">Submitting...</span>
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .public-contact-wrapper {
      min-height: 90vh;
      background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem 1rem;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .contact-card {
      background: white;
      border-radius: 16px;
      box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1);
      width: 100%;
      max-width: 680px;
      padding: 2.5rem;
      border: 1px solid #e2e8f0;
    }
    .contact-header { text-align: center; margin-bottom: 2rem; }
    .store-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      background: #eff6ff;
      color: #2563eb;
      font-size: 0.8rem;
      font-weight: 700;
      padding: 0.3rem 0.75rem;
      border-radius: 9999px;
      margin-bottom: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    h1 { font-size: 1.85rem; font-weight: 800; color: #0f172a; margin: 0 0 0.5rem 0; }
    .subtitle { color: #64748b; font-size: 0.95rem; line-height: 1.5; margin: 0; }

    .inquiry-form { display: flex; flex-direction: column; gap: 1.25rem; }
    .form-row { display: flex; gap: 1rem; }
    @media (max-width: 600px) {
      .form-row { flex-direction: column; }
    }
    .form-group.col { flex: 1; }
    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #334155; margin-bottom: 0.4rem; }
    .form-group input, .form-group select, .form-group textarea {
      width: 100%;
      padding: 0.65rem 0.85rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 0.9rem;
      box-sizing: border-box;
      transition: border-color 0.15s ease;
    }
    .form-group input:focus, .form-group select:focus, .form-group textarea:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 0.75rem 1.5rem;
      border-radius: 8px;
      font-size: 0.95rem;
      font-weight: 700;
      cursor: pointer;
      border: none;
    }
    .btn-primary { background: #2563eb; color: white; }
    .btn-primary:hover { background: #1d4ed8; }
    .btn-secondary { background: #f1f5f9; color: #334155; }
    .btn-secondary:hover { background: #e2e8f0; }
    .btn-submit { width: 100%; margin-top: 0.5rem; }

    .error-msg { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: 6px; font-size: 0.85rem; }

    .success-banner {
      text-align: center;
      padding: 2rem 1rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
    }
    .success-banner i { font-size: 3.5rem; color: #10b981; }
    .success-banner h2 { font-size: 1.5rem; font-weight: 700; color: #0f172a; margin: 0; }
    .success-banner p { color: #475569; font-size: 0.95rem; max-width: 450px; line-height: 1.5; margin: 0; }
  `]
})
export class PublicContactComponent implements OnInit {
  tenantSlug = 'evergreen';
  inquiry: PublicInquiryRequest = {
    name: '',
    company: '',
    email: '',
    phone: '',
    eventType: 'WEDDING',
    eventDate: '',
    message: '',
    honeypot: ''
  };

  submitting = false;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private crmService: CrmService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['tenantSlug']) {
        this.tenantSlug = params['tenantSlug'];
      }
    });
  }

  onSubmit(): void {
    this.submitting = true;
    this.errorMessage = '';
    this.crmService.submitPublicInquiry(this.tenantSlug, this.inquiry).subscribe({
      next: (res) => {
        this.submitting = false;
        this.submitted = true;
        this.successMessage = res.message || 'We received your inquiry and our sales team will follow up promptly.';
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.error || 'Failed to submit inquiry. Please try again.';
      }
    });
  }

  resetForm(): void {
    this.inquiry = {
      name: '',
      company: '',
      email: '',
      phone: '',
      eventType: 'WEDDING',
      eventDate: '',
      message: '',
      honeypot: ''
    };
    this.submitted = false;
  }
}
