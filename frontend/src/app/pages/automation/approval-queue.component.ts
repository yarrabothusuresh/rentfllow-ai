import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { AutomationApproval } from './automation.model';

@Component({
  selector: 'app-approval-queue',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './approval-queue.component.html',
  styleUrls: ['./approval-queue.component.scss']
})
export class ApprovalQueueComponent implements OnInit {
  approvals: AutomationApproval[] = [];
  filteredApprovals: AutomationApproval[] = [];
  loading: boolean = false;
  selectedRole: string = 'ADMIN';

  // Modal confirmation
  activeApproval: AutomationApproval | null = null;
  actionType: 'APPROVE' | 'REJECT' = 'APPROVE';
  reason: string = '';
  submitting: boolean = false;
  message: string | null = null;

  // Filter
  statusFilter: string = 'PENDING';

  constructor(private automationService: AutomationService) {}

  ngOnInit(): void {
    this.loadApprovals();
  }

  loadApprovals(): void {
    this.loading = true;
    this.automationService.getApprovals(undefined, 0, 50, this.selectedRole).subscribe({
      next: (data) => {
        this.approvals = data.content || [];
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load approvals', err);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.statusFilter === 'ALL') {
      this.filteredApprovals = [...this.approvals];
    } else {
      this.filteredApprovals = this.approvals.filter(a => a.status === this.statusFilter);
    }
  }

  openModal(approval: AutomationApproval, action: 'APPROVE' | 'REJECT'): void {
    this.activeApproval = approval;
    this.actionType = action;
    this.reason = action === 'APPROVE' ? 'Approved by operations manager' : 'Declined as not required';
  }

  closeModal(): void {
    this.activeApproval = null;
  }

  submitDecision(): void {
    if (!this.activeApproval) return;
    this.submitting = true;

    if (this.actionType === 'APPROVE') {
      this.automationService.approve(this.activeApproval.id, this.reason, this.selectedRole).subscribe({
        next: () => {
          this.submitting = false;
          this.closeModal();
          this.message = `Approved and executed action successfully!`;
          this.loadApprovals();
        },
        error: (err) => {
          this.submitting = false;
          alert('Approval failed: ' + (err.error?.error || err.message));
        }
      });
    } else {
      this.automationService.reject(this.activeApproval.id, this.reason, this.selectedRole).subscribe({
        next: () => {
          this.submitting = false;
          this.closeModal();
          this.message = `Action rejected.`;
          this.loadApprovals();
        },
        error: (err) => {
          this.submitting = false;
          alert('Rejection failed: ' + (err.error?.error || err.message));
        }
      });
    }
  }
}
