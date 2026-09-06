import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AutomationService } from './automation.service';
import { AiRecommendation } from './automation.model';

@Component({
  selector: 'app-recommendations-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './recommendations-list.component.html',
  styleUrls: ['./recommendations-list.component.scss']
})
export class RecommendationsListComponent implements OnInit {
  recommendations: AiRecommendation[] = [];
  filteredRecommendations: AiRecommendation[] = [];
  loading: boolean = false;
  selectedRole: string = 'ADMIN';

  // Filters
  statusFilter: string = 'ALL';
  priorityFilter: string = 'ALL';
  categoryFilter: string = 'ALL';
  searchQuery: string = '';

  constructor(private automationService: AutomationService) {}

  ngOnInit(): void {
    this.loadRecommendations();
  }

  loadRecommendations(): void {
    this.loading = true;
    this.automationService.getRecommendations(undefined, undefined, undefined, 0, 50, this.selectedRole).subscribe({
      next: (data) => {
        this.recommendations = data.content || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load recommendations', err);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredRecommendations = this.recommendations.filter(rec => {
      const matchStatus = this.statusFilter === 'ALL' || rec.status === this.statusFilter;
      const matchPriority = this.priorityFilter === 'ALL' || rec.priority === this.priorityFilter;
      const matchCategory = this.categoryFilter === 'ALL' || rec.category === this.categoryFilter;
      const matchSearch = !this.searchQuery || 
        rec.recommendationNumber.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        rec.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        (rec.entityReference && rec.entityReference.toLowerCase().includes(this.searchQuery.toLowerCase()));
      return matchStatus && matchPriority && matchCategory && matchSearch;
    });
  }

  dismiss(rec: AiRecommendation): void {
    const reason = prompt('Please enter reason for dismissing this recommendation:', 'No action required');
    if (!reason) return;

    this.automationService.dismissRecommendation(rec.id, reason, this.selectedRole).subscribe({
      next: () => {
        this.loadRecommendations();
      },
      error: (err) => alert('Dismissal failed: ' + err.message)
    });
  }
}
