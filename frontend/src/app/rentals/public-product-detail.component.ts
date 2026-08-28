import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StorefrontService, PublicProduct, PublicAvailabilityResponse } from '../services/storefront.service';

@Component({
  selector: 'app-public-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="product-detail-layout">
      <!-- Top Navigation -->
      <nav class="detail-nav">
        <a routerLink="/rentals" class="back-link">← Back to Catalog</a>
        <div class="nav-right">
          <button class="cart-btn" routerLink="/rentals/cart">🛒 Cart</button>
        </div>
      </nav>

      <div class="detail-container" *ngIf="product">
        <!-- Left: Image Gallery -->
        <div class="image-section">
          <div class="main-image">
            <img [src]="product.imageUrl || 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=600&q=80'" [alt]="product.name" />
          </div>
        </div>

        <!-- Right: Details & Booking Widget -->
        <div class="info-section">
          <span class="category-tag">{{ product.categoryName || 'Rental Equipment' }}</span>
          <h1 class="product-title">{{ product.name }}</h1>
          <p class="sku">SKU: {{ product.sku }}</p>

          <div class="price-box">
            <span class="price-amount">\${{ product.rentalPrice | number:'1.2-2' }}</span>
            <span class="price-unit">/ rental period</span>
          </div>

          <p class="description">{{ product.description || 'Premium commercial grade equipment suitable for indoor & outdoor events.' }}</p>

          <div class="widget-card">
            <h3>SELECT RENTAL DATES</h3>
            
            <div class="date-grid">
              <div class="form-group">
                <label>Start Date</label>
                <input type="date" [(ngModel)]="startDate" (change)="checkAvailability()" class="form-control" />
              </div>
              <div class="form-group">
                <label>End Date</label>
                <input type="date" [(ngModel)]="endDate" (change)="checkAvailability()" class="form-control" />
              </div>
            </div>

            <div class="form-group">
              <label>Quantity</label>
              <input type="number" [(ngModel)]="quantity" (change)="checkAvailability()" min="1" class="form-control" />
            </div>

            <!-- Availability Status Badge -->
            <div class="availability-result" *ngIf="availability">
              <div class="status-indicator" [class.available]="availability.available" [class.unavailable]="!availability.available">
                <span class="icon">{{ availability.available ? '✓' : '⚠️' }}</span>
                <div>
                  <strong>{{ availability.available ? 'Available' : 'Quantity Limited' }}</strong>
                  <p class="subtext">
                    {{ availability.available ? 'Requested ' + quantity + ' items available for selected dates.' : 'Only ' + availability.availableQuantity + ' available for selected dates.' }}
                  </p>
                </div>
              </div>
            </div>

            <div class="action-buttons">
              <button class="btn-add" (click)="addToCart()" [disabled]="availability && !availability.available">
                Add to Rental
              </button>
              <button class="btn-quote" (click)="requestQuote()">
                Request Quote
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-detail-layout {
      min-height: 100vh;
      background: #f8fafc;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .detail-nav {
      background: #1e293b;
      padding: 1rem 3rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .back-link {
      color: #94a3b8;
      text-decoration: none;
      font-weight: 600;
    }
    .back-link:hover { color: white; }
    .cart-btn {
      background: #3b82f6;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
    }
    .detail-container {
      max-width: 1200px;
      margin: 3rem auto;
      padding: 0 2rem;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 3rem;
    }
    .main-image {
      background: white;
      border-radius: 16px;
      overflow: hidden;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
      border: 1px solid #e2e8f0;
      height: 420px;
    }
    .main-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .category-tag {
      background: #e2e8f0;
      color: #475569;
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .product-title {
      font-size: 2.25rem;
      font-weight: 800;
      color: #0f172a;
      margin: 0.5rem 0 0.25rem 0;
    }
    .sku {
      color: #94a3b8;
      font-size: 0.85rem;
      margin-bottom: 1rem;
    }
    .price-box {
      margin-bottom: 1.5rem;
    }
    .price-amount {
      font-size: 2.25rem;
      font-weight: 800;
      color: #2563eb;
    }
    .price-unit {
      color: #64748b;
      font-size: 0.9rem;
      margin-left: 0.5rem;
    }
    .description {
      color: #475569;
      line-height: 1.6;
      margin-bottom: 2rem;
    }
    .widget-card {
      background: white;
      padding: 1.75rem;
      border-radius: 16px;
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
      border: 1px solid #cbd5e1;
    }
    .widget-card h3 {
      margin-top: 0;
      font-size: 1rem;
      color: #0f172a;
      letter-spacing: 0.05em;
    }
    .date-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    .form-group label {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: #475569;
      margin-bottom: 0.35rem;
    }
    .form-control {
      width: 100%;
      padding: 0.65rem;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      box-sizing: border-box;
    }
    .availability-result {
      margin: 1.25rem 0;
    }
    .status-indicator {
      display: flex;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      border-radius: 8px;
    }
    .status-indicator.available {
      background: #dcfce7;
      border: 1px solid #86efac;
      color: #166534;
    }
    .status-indicator.unavailable {
      background: #fee2e2;
      border: 1px solid #fca5a5;
      color: #991b1b;
    }
    .subtext {
      margin: 0.2rem 0 0 0;
      font-size: 0.8rem;
    }
    .action-buttons {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-top: 1.5rem;
    }
    .btn-add {
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.85rem;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
    }
    .btn-add:disabled {
      background: #94a3b8;
      cursor: not-allowed;
    }
    .btn-quote {
      background: #0f172a;
      color: white;
      border: none;
      padding: 0.85rem;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
    }

    @media (max-width: 900px) {
      .detail-container {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class PublicProductDetailComponent implements OnInit {
  productId: string = '';
  product: PublicProduct | null = null;

  startDate: string = new Date(Date.now() + 86400000).toISOString().split('T')[0];
  endDate: string = new Date(Date.now() + 86400000 * 3).toISOString().split('T')[0];
  quantity: number = 50;

  availability: PublicAvailabilityResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private storefrontService: StorefrontService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productId = this.route.snapshot.paramMap.get('productId') || '';
    if (this.productId) {
      this.loadProduct();
    }
  }

  loadProduct(): void {
    this.storefrontService.getProductDetail(this.productId).subscribe({
      next: (p) => {
        this.product = p;
        this.checkAvailability();
      }
    });
  }

  checkAvailability(): void {
    if (!this.productId) return;
    this.storefrontService.checkAvailability(this.productId, this.startDate, this.endDate, this.quantity).subscribe({
      next: (res) => {
        this.availability = res;
      }
    });
  }

  addToCart(): void {
    if (!this.productId) return;
    this.storefrontService.addToCart(this.productId, this.quantity, this.startDate + 'T09:00:00', this.endDate + 'T18:00:00').subscribe({
      next: () => {
        this.router.navigate(['/rentals/cart']);
      }
    });
  }

  requestQuote(): void {
    this.addToCart();
    this.router.navigate(['/rentals/request-quote']);
  }
}
