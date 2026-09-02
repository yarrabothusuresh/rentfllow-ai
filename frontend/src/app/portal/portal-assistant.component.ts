import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CustomerChatWidgetComponent } from '../pages/ai-sales/customer-chat-widget.component';

@Component({
  selector: 'app-portal-assistant',
  standalone: true,
  imports: [CommonModule, RouterModule, CustomerChatWidgetComponent],
  template: `
    <div class="portal-assistant-page">
      <div class="header-container">
        <div>
          <h2><i class="bi bi-chat-heart"></i> AI Rental Assistant</h2>
          <p class="subtitle">Get instant recommendations, check live date availability, and receive a draft rental quote.</p>
        </div>
      </div>
      <app-customer-chat-widget></app-customer-chat-widget>
    </div>
  `,
  styles: [`
    .portal-assistant-page {
      padding: 1.5rem;
      max-width: 900px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .header-container { text-align: center; margin-bottom: 1.5rem; }
    h2 { font-size: 1.6rem; font-weight: 700; color: #0f172a; margin: 0; }
    .subtitle { color: #64748b; font-size: 0.95rem; margin-top: 0.35rem; }
  `]
})
export class PortalAssistantComponent {}
