import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AiSalesService } from '../../services/ai-sales.service';
import { AiSettings } from '../../models/ai-sales.model';

@Component({
  selector: 'app-ai-sales-settings',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="settings-container">
      <header class="page-header">
        <div>
          <div class="breadcrumb">
            <a routerLink="/ai-sales">&larr; AI Sales Console</a>
          </div>
          <h1>AI Sales Agent Configuration</h1>
          <p class="subtitle">Configure AI model orchestration, provider selection, margin policies, and approval thresholds.</p>
        </div>
        <div class="save-status" *ngIf="savedMessage">
          <i class="bi bi-check-circle-fill"></i> {{ savedMessage }}
        </div>
      </header>

      <div class="settings-grid" *ngIf="settings">
        <!-- Provider & Models -->
        <section class="card">
          <h2><i class="bi bi-cpu"></i> AI Provider & Engine</h2>
          <p class="section-desc">Choose between the zero-cost deterministic mock engine or external LLM APIs.</p>

          <div class="form-group">
            <label>Master AI Switch:</label>
            <label class="toggle-switch">
              <input type="checkbox" [(ngModel)]="settings.aiEnabled" />
              <span class="slider round"></span>
              <span class="toggle-label">{{ settings.aiEnabled ? 'Enabled' : 'Disabled' }}</span>
            </label>
          </div>

          <div class="form-group">
            <label>AI Provider:</label>
            <select [(ngModel)]="settings.aiProvider">
              <option value="mock">Mock AI Engine (Deterministic & Zero-Cost)</option>
              <option value="openai">OpenAI / Compatible REST API</option>
              <option value="gemini">Google Gemini AI</option>
            </select>
          </div>

          <div class="form-group">
            <label>Model Version:</label>
            <select [(ngModel)]="settings.aiModel">
              <option value="mock-sales-v1">mock-sales-v1 (Recommended for demos & testing)</option>
              <option value="gpt-4o-mini">gpt-4o-mini</option>
              <option value="gpt-4o">gpt-4o</option>
              <option value="gemini-1.5-flash">gemini-1.5-flash</option>
            </select>
          </div>
        </section>

        <!-- Channels & Authorization -->
        <section class="card">
          <h2><i class="bi bi-sliders"></i> Channel Availability & Approvals</h2>
          <p class="section-desc">Control where autonomous discovery operates and enforce human review.</p>

          <div class="form-group">
            <label>Customer Self-Service Chat (Portal & Storefront):</label>
            <label class="toggle-switch">
              <input type="checkbox" [(ngModel)]="settings.customerAiEnabled" />
              <span class="slider round"></span>
              <span class="toggle-label">{{ settings.customerAiEnabled ? 'Active' : 'Deactivated' }}</span>
            </label>
          </div>

          <div class="form-group">
            <label>Internal Staff Assistant (CRM Console):</label>
            <label class="toggle-switch">
              <input type="checkbox" [(ngModel)]="settings.internalSalesAssistantEnabled" />
              <span class="slider round"></span>
              <span class="toggle-label">{{ settings.internalSalesAssistantEnabled ? 'Active' : 'Deactivated' }}</span>
            </label>
          </div>

          <div class="form-group alert-box">
            <div class="alert-title"><i class="bi bi-shield-check"></i> Human Approval Mandate</div>
            <label class="toggle-switch">
              <input type="checkbox" [(ngModel)]="settings.humanQuoteApprovalRequired" />
              <span class="slider round"></span>
              <span class="toggle-label">Require sales representative review before commercial quotes are sent</span>
            </label>
            <small>Guarantees AI drafts remain in DRAFT status until confirmed by sales personnel.</small>
          </div>
        </section>

        <!-- Margin & Financial Safeguards -->
        <section class="card">
          <h2><i class="bi bi-graph-up"></i> Profitability & Margin Guardrails</h2>
          <p class="section-desc">Internal margin thresholds used during tool verification.</p>

          <div class="form-group">
            <label>Target Gross Margin (%):</label>
            <input type="number" [(ngModel)]="settings.targetGrossMarginPct" min="1" max="100" />
            <small>Quotes meeting or exceeding this margin are marked HEALTHY.</small>
          </div>

          <div class="form-group">
            <label>Low Margin Warning Threshold (%):</label>
            <input type="number" [(ngModel)]="settings.lowMarginThresholdPct" min="1" max="100" />
            <small>Quotes below this threshold generate internal warning flags for the sales representative.</small>
          </div>

          <div class="form-group">
            <label>Daily Customer Request Quota:</label>
            <input type="number" [(ngModel)]="settings.dailyRequestLimit" min="10" max="10000" />
            <small>Protects against volumetric abuse or rapid bot requests.</small>
          </div>
        </section>
      </div>

      <div class="footer-save" *ngIf="settings">
        <button (click)="saveSettings()" class="btn btn-primary btn-lg" [disabled]="isSaving">
          <i class="bi bi-check2-circle"></i> {{ isSaving ? 'Saving...' : 'Save Configuration' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .settings-container {
      padding: 1.5rem;
      max-width: 1100px;
      margin: 0 auto;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    .breadcrumb a { color: #3b82f6; text-decoration: none; font-size: 0.85rem; font-weight: 500; }
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 1rem;
    }
    h1 { font-size: 1.75rem; font-weight: 700; color: #0f172a; margin: 0.25rem 0 0 0; }
    .subtitle { color: #64748b; margin-top: 0.25rem; font-size: 0.95rem; }

    .save-status {
      background: #ecfdf5;
      color: #065f46;
      border: 1px solid #a7f3d0;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-weight: 600;
      font-size: 0.85rem;
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }

    .settings-grid { display: flex; flex-direction: column; gap: 1.5rem; }

    .card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.04);
    }
    .card h2 {
      font-size: 1.15rem;
      font-weight: 600;
      color: #0f172a;
      margin: 0 0 0.25rem 0;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .section-desc { font-size: 0.85rem; color: #64748b; margin: 0 0 1.25rem 0; }

    .form-group { margin-bottom: 1.25rem; }
    .form-group label { display: block; font-size: 0.875rem; font-weight: 600; color: #334155; margin-bottom: 0.4rem; }
    .form-group select, .form-group input[type="number"] {
      width: 100%;
      max-width: 450px;
      padding: 0.55rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.875rem;
      background: white;
    }
    .form-group small { display: block; font-size: 0.75rem; color: #94a3b8; margin-top: 0.35rem; }

    .alert-box {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      padding: 1rem;
      border-radius: 8px;
    }
    .alert-title { font-size: 0.875rem; font-weight: 700; color: #1e293b; margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.4rem; }

    /* Toggle switch */
    .toggle-switch { display: inline-flex; align-items: center; gap: 0.6rem; cursor: pointer; position: relative; }
    .toggle-switch input { opacity: 0; width: 0; height: 0; }
    .slider {
      position: relative;
      display: inline-block;
      width: 44px;
      height: 24px;
      background-color: #cbd5e1;
      transition: .3s;
      border-radius: 24px;
    }
    .slider:before {
      position: absolute;
      content: "";
      height: 18px;
      width: 18px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: .3s;
      border-radius: 50%;
    }
    input:checked + .slider { background-color: #3b82f6; }
    input:checked + .slider:before { transform: translateX(20px); }
    .toggle-label { font-size: 0.875rem; color: #334155; font-weight: 500; }

    .footer-save {
      margin-top: 1.5rem;
      display: flex;
      justify-content: flex-end;
    }
    .btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      border: 1px solid transparent;
    }
    .btn-lg { padding: 0.65rem 1.5rem; font-size: 0.95rem; }
    .btn-primary { background: #3b82f6; color: white; }
    .btn-primary:hover { background: #2563eb; }
  `]
})
export class AiSalesSettingsComponent implements OnInit {
  settings: AiSettings | null = null;
  isSaving = false;
  savedMessage = '';

  constructor(private aiSalesService: AiSalesService) {}

  ngOnInit(): void {
    this.aiSalesService.getSettings().subscribe({
      next: (s) => this.settings = s,
      error: (err) => console.error('Failed to load AI settings', err)
    });
  }

  saveSettings(): void {
    if (!this.settings) return;
    this.isSaving = true;
    this.aiSalesService.updateSettings(this.settings).subscribe({
      next: (updated) => {
        this.settings = updated;
        this.isSaving = false;
        this.savedMessage = 'Settings saved successfully.';
        setTimeout(() => this.savedMessage = '', 3500);
      },
      error: (err) => {
        console.error('Failed to save AI settings', err);
        this.isSaving = false;
      }
    });
  }
}
