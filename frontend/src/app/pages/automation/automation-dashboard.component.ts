import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { 
  AiRecommendation, 
  AutomationApproval, 
  AutomationDashboardDTO, 
  AutomationExecution 
} from './automation.model';

@Component({
  selector: 'app-automation-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './automation-dashboard.component.html',
  styleUrls: ['./automation-dashboard.component.scss']
})
export class AutomationDashboardComponent implements OnInit {

  dashboard: AutomationDashboardDTO | null = null;
  loading: boolean = false;
  scanning: boolean = false;
  actionMessage: string | null = null;
  selectedRole: string = 'ADMIN';

  // Modal state
  activeApproval: AutomationApproval | null = null;
  approvalActionType: 'APPROVE' | 'REJECT' = 'APPROVE';
  approvalReason: string = '';
  modalSubmitting: boolean = false;

  constructor(private automationService: AutomationService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.automationService.getDashboard(this.selectedRole).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load automation dashboard', err);
        this.loading = false;
      }
    });
  }

  triggerScan(): void {
    this.scanning = true;
    this.actionMessage = null;
    this.automationService.triggerDetection(this.selectedRole).subscribe({
      next: (recs) => {
        this.scanning = false;
        this.actionMessage = `Proactive scan completed: ${recs.length} active recommendation(s) processed.`;
        this.loadDashboard();
      },
      error: (err) => {
        this.scanning = false;
        this.actionMessage = 'Scan encountered an error. Please try again.';
      }
    });
  }

  openApprovalModal(approval: AutomationApproval, action: 'APPROVE' | 'REJECT'): void {
    this.activeApproval = approval;
    this.approvalActionType = action;
    this.approvalReason = action === 'APPROVE' ? 'Approved after review' : 'Action not required';
  }

  closeApprovalModal(): void {
    this.activeApproval = null;
  }

  submitApprovalDecision(): void {
    if (!this.activeApproval) return;
    this.modalSubmitting = true;

    if (this.approvalActionType === 'APPROVE') {
      this.automationService.approve(this.activeApproval.id, this.approvalReason, this.selectedRole).subscribe({
        next: () => {
          this.modalSubmitting = false;
          this.closeApprovalModal();
          this.actionMessage = `Action successfully approved & executed!`;
          this.loadDashboard();
        },
        error: (err) => {
          this.modalSubmitting = false;
          this.actionMessage = `Approval failed: ${err.error?.error || err.message}`;
        }
      });
    } else {
      this.automationService.reject(this.activeApproval.id, this.approvalReason, this.selectedRole).subscribe({
        next: () => {
          this.modalSubmitting = false;
          this.closeApprovalModal();
          this.actionMessage = `Action proposal rejected.`;
          this.loadDashboard();
        },
        error: (err) => {
          this.modalSubmitting = false;
          this.actionMessage = `Rejection failed: ${err.error?.error || err.message}`;
        }
      });
    }
  }

  dismissRecommendation(rec: AiRecommendation): void {
    const reason = prompt('Please enter reason for dismissing this recommendation:', 'No action required at this time');
    if (!reason) return;

    this.automationService.dismissRecommendation(rec.id, reason, this.selectedRole).subscribe({
      next: () => {
        this.actionMessage = `Recommendation ${rec.recommendationNumber} dismissed.`;
        this.loadDashboard();
      },
      error: (err) => {
        this.actionMessage = `Dismissal failed: ${err.message}`;
      }
    });
  }
}
