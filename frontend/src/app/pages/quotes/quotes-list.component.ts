import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuoteService } from '../../services/quote.service';
import { Quote, QuoteStatus } from '../../models/quote.models';

@Component({
  selector: 'app-quotes-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './quotes-list.component.html',
  styleUrls: ['./quotes-list.component.css']
})
export class QuotesListComponent implements OnInit {
  quotes: Quote[] = [];
  filteredQuotes: Quote[] = [];
  isLoading = true;
  searchTerm = '';
  selectedStatus = 'ALL';

  // Metrics
  totalQuotes = 0;
  draftCount = 0;
  sentCount = 0;
  acceptedCount = 0;
  totalPipelineValue = 0;

  constructor(private quoteService: QuoteService, private router: Router) {}

  ngOnInit(): void {
    this.loadQuotes();
  }

  loadQuotes(): void {
    this.isLoading = true;
    this.quoteService.getQuotes().subscribe({
      next: (data) => {
        this.quotes = data;
        this.calculateMetrics();
        this.filterQuotes();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load quotes', err);
        this.isLoading = false;
      }
    });
  }

  calculateMetrics(): void {
    this.totalQuotes = this.quotes.length;
    this.draftCount = this.quotes.filter(q => q.status === 'DRAFT').length;
    this.sentCount = this.quotes.filter(q => q.status === 'SENT' || q.status === 'VIEWED').length;
    this.acceptedCount = this.quotes.filter(q => q.status === 'ACCEPTED').length;
    this.totalPipelineValue = this.quotes.reduce((sum, q) => sum + (q.totalAmount || 0), 0);
  }

  filterQuotes(): void {
    this.filteredQuotes = this.quotes.filter(q => {
      const matchesSearch = !this.searchTerm ||
        q.quoteNumber?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        q.customerName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        q.eventName?.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesStatus = this.selectedStatus === 'ALL' || q.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }

  onSearchChange(): void {
    this.filterQuotes();
  }

  onStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.filterQuotes();
  }

  duplicateQuote(id: string, event: Event): void {
    event.stopPropagation();
    this.quoteService.duplicateQuote(id).subscribe({
      next: (dup) => {
        this.loadQuotes();
        this.router.navigate(['/quotes', dup.id, 'edit']);
      },
      error: (err) => alert('Failed to duplicate quote: ' + err.message)
    });
  }

  getStatusBadgeClass(status: QuoteStatus): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'PENDING_REVIEW': return 'badge-review';
      case 'SENT': return 'badge-sent';
      case 'VIEWED': return 'badge-viewed';
      case 'ACCEPTED': return 'badge-accepted';
      case 'REJECTED': return 'badge-rejected';
      case 'EXPIRED': return 'badge-expired';
      case 'CANCELLED': return 'badge-cancelled';
      default: return 'badge-default';
    }
  }
}
