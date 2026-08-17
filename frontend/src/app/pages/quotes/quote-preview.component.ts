import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { Quote } from '../../models/quote.models';

@Component({
  selector: 'app-quote-preview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-preview.component.html',
  styleUrls: ['./quote-preview.component.css']
})
export class QuotePreviewComponent implements OnInit {
  quoteId: string | null = null;
  quote: Quote | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private quoteService: QuoteService
  ) {}

  ngOnInit(): void {
    this.quoteId = this.route.snapshot.paramMap.get('id');
    if (this.quoteId) {
      // Fetch quote with CUSTOMER role header to test redaction
      this.quoteService.getQuoteById(this.quoteId, 'CUSTOMER').subscribe({
        next: (data) => {
          this.quote = data;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load customer preview quote', err);
          this.isLoading = false;
        }
      });
    }
  }

  printQuote(): void {
    window.print();
  }
}
