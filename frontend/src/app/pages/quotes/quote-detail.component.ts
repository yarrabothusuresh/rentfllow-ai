import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { Quote, QuoteStatus } from '../../models/quote.models';

@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-detail.component.html',
  styleUrls: ['./quote-detail.component.css']
})
export class QuoteDetailComponent implements OnInit {
  quoteId: string | null = null;
  quote: Quote | null = null;
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quoteService: QuoteService
  ) {}

  ngOnInit(): void {
    this.quoteId = this.route.snapshot.paramMap.get('id');
    if (this.quoteId) {
      this.loadQuote(this.quoteId);
    }
  }

  loadQuote(id: string): void {
    this.isLoading = true;
    this.quoteService.getQuoteById(id).subscribe({
      next: (data) => {
        this.quote = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load quote details: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  updateStatus(newStatus: QuoteStatus): void {
    if (!this.quoteId) return;
    this.quoteService.updateStatus(this.quoteId, newStatus).subscribe({
      next: (updated) => {
        this.quote = updated;
      },
      error: (err) => alert('Failed to update quote status: ' + err.message)
    });
  }

  duplicateQuote(): void {
    if (!this.quoteId) return;
    this.quoteService.duplicateQuote(this.quoteId).subscribe({
      next: (dup) => {
        this.router.navigate(['/quotes', dup.id, 'edit']);
      },
      error: (err) => alert('Failed to duplicate quote: ' + err.message)
    });
  }

  getStatusBadgeClass(status?: QuoteStatus): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'SENT': return 'badge-sent';
      case 'VIEWED': return 'badge-viewed';
      case 'ACCEPTED': return 'badge-accepted';
      case 'REJECTED': return 'badge-rejected';
      case 'EXPIRED': return 'badge-expired';
      default: return 'badge-default';
    }
  }
}
