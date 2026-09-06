import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { AiRecommendation, AutomationApproval } from './automation.model';

@Component({
  selector: 'app-recommendation-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './recommendation-detail.component.html',
  styleUrls: ['./recommendation-detail.component.scss']
})
export class RecommendationDetailComponent implements OnInit {
  recommendationId: string | null = null;
  recommendation: AiRecommendation | null = null;
  loading: boolean = false;
  selectedRole: string = 'ADMIN';

  // Action proposal state
  proposing: boolean = false;
  proposalSuccessMessage: string | null = null;
  createdApproval: AutomationApproval | null = null;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private automationService: AutomationService
  ) {}

  ngOnInit(): void {
    this.recommendationId = this.route.snapshot.paramMap.get('id');
    if (this.recommendationId) {
      this.loadRecommendation(this.recommendationId);
    }
  }

  loadRecommendation(id: string): void {
    this.loading = true;
    this.automationService.getRecommendation(id, this.selectedRole).subscribe({
      next: (data) => {
        this.recommendation = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load recommendation: ' + (err.error?.error || err.message);
        this.loading = false;
      }
    });
  }

  proposeAction(): void {
    if (!this.recommendation) return;
    this.proposing = true;
    this.errorMessage = null;

    this.automationService.proposeAction(this.recommendation.id, this.selectedRole).subscribe({
      next: (approval) => {
        this.proposing = false;
        this.createdApproval = approval;
        this.proposalSuccessMessage = `Action proposed successfully! Transferred to approval queue (ID: ${approval.id.substring(0, 8)}...).`;
        this.loadRecommendation(this.recommendation!.id);
      },
      error: (err) => {
        this.proposing = false;
        this.errorMessage = 'Failed to propose action: ' + (err.error?.error || err.message);
      }
    });
  }

  dismiss(): void {
    if (!this.recommendation) return;
    const reason = prompt('Please enter reason for dismissing this recommendation:', 'Not applicable at this time');
    if (!reason) return;

    this.automationService.dismissRecommendation(this.recommendation.id, reason, this.selectedRole).subscribe({
      next: () => {
        this.router.navigate(['/dashboard/automation/recommendations']);
      },
      error: (err) => alert('Dismissal failed: ' + err.message)
    });
  }

  getEvidenceEntries(rec: AiRecommendation): { key: string; value: any }[] {
    let ev = rec.evidence;
    if (!ev && rec.evidenceJson) {
      try {
        ev = JSON.parse(rec.evidenceJson);
      } catch (e) {
        ev = null;
      }
    }
    if (!ev || typeof ev !== 'object') return [];
    return Object.entries(ev).map(([key, value]) => ({ key, value }));
  }
}
