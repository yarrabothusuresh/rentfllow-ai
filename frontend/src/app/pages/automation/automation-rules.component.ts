import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { AutomationRule } from './automation.model';

@Component({
  selector: 'app-automation-rules',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './automation-rules.component.html',
  styleUrls: ['./automation-rules.component.scss']
})
export class AutomationRulesComponent implements OnInit {
  rules: AutomationRule[] = [];
  loading: boolean = false;
  selectedRole: string = 'ADMIN';
  message: string | null = null;

  constructor(private automationService: AutomationService) {}

  ngOnInit(): void {
    this.loadRules();
  }

  loadRules(): void {
    this.loading = true;
    this.automationService.getRules(this.selectedRole).subscribe({
      next: (data) => {
        this.rules = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load rules', err);
        this.loading = false;
      }
    });
  }

  toggleEnabled(rule: AutomationRule): void {
    this.automationService.toggleRule(rule.id, !rule.enabled, this.selectedRole).subscribe({
      next: (updated: AutomationRule) => {
        rule.enabled = updated.enabled;
        this.message = `Rule '${rule.name}' is now ${rule.enabled ? 'Enabled' : 'Disabled'}.`;
      },
      error: (err: any) => alert('Failed to update rule: ' + (err.error?.error || err.message))
    });
  }

  updateMode(rule: AutomationRule, newMode: 'RECOMMEND_ONLY' | 'APPROVAL_REQUIRED' | 'AUTO_EXECUTE_LOW_RISK'): void {
    this.automationService.updateRuleMode(rule.id, newMode, this.selectedRole).subscribe({
      next: (updated: AutomationRule) => {
        rule.mode = updated.mode;
        this.message = `Updated mode for '${rule.name}' to ${rule.mode}.`;
      },
      error: (err: any) => alert('Failed to update rule mode: ' + (err.error?.error || err.message))
    });
  }
}
