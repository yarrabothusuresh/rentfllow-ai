import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StorefrontService, PublicProduct, TenantStorefront } from '../services/storefront.service';

@Component({
  selector: 'app-public-catalog',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="storefront-layout">
      <!-- Header / Banner -->
      <header class="storefront-header">
        <div class="brand-container">
          <h1 class="brand-title">{{ storefront?.companyName || 'ABC Event Rentals' }}</h1>
          <p class="brand-subtitle">Browse premium rental inventory, check live availability, & book online</p>
        </div>
        <div class="header-actions">
          <button class="cart-btn" routerLink="/rentals/cart">
            🛒 Cart
          </button>

          <button class="portal-btn" routerLink="/portal">
            🔒 Customer Portal
          </button>
        </div>
      </header>

      <!-- Main Container -->
      <div class="catalog-container">
        <!-- Filter Controls -->
        <aside class="filter-sidebar">
          <h3>Filter & Search</h3>
          
          <div class="filter-group">
            <label>Search Products</label>
            <input type="text" [(ngModel)]="searchTerm" (input)="onFilterChange()" placeholder="Search chairs, tables, linens..." class="form-control" />
          </div>

          <div class="filter-group">
            <label>Category</label>
            <select [(ngModel)]="selectedCategory" (change)="onFilterChange()" class="form-control">
              <option value="">All Categories</option>
              <option value="Seating">Seating</option>
              <option value="Tables">Tables</option>
              <option value="Linens">Linens</option>
              <option value="Lighting">Lighting</option>
              <option value="Audio/Visual">Audio/Visual</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Sort By</label>
            <select [(ngModel)]="sortBy" (change)="onFilterChange()" class="form-control">
              <option value="RECOMMENDED">Recommended</option>
              <option value="PRICE_ASC">Price: Low to High</option>
              <option value="PRICE_DESC">Price: High to Low</option>
              <option value="NAME">Name: A-Z</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Price Range</label>
            <div class="price-inputs">
              <input type="number" [(ngModel)]="minPrice" (change)="onFilterChange()" placeholder="Min $" class="form-control" />
              <input type="number" [(ngModel)]="maxPrice" (change)="onFilterChange()" placeholder="Max $" class="form-control" />
            </div>
          </div>
        </aside>

        <!-- Product Grid -->
        <main class="product-grid-section">
          <div class="grid-header">
            <h2>Rental Products</h2>
            <span class="results-count">{{ products.length }} items available</span>
          </div>

          <div *ngIf="loading" class="loading-state">
            <p>Loading available rental items...</p>
          </div>

          <div *ngIf="!loading && products.length === 0" class="empty-state">
            <p>No products match your current filters.</p>
          </div>

          <div class="product-grid" *ngIf="!loading && products.length > 0">
            <div class="product-card" *ngFor="let p of products">
              <div class="card-image-wrapper">
                <img [src]="p.imageUrl || 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=400&q=80'" [alt]="p.name" />
                <span class="category-badge">{{ p.categoryName || 'Equipment' }}</span>
              </div>
              <div class="card-content">
                <h3>{{ p.name }}</h3>
                <p class="sku">SKU: {{ p.sku }}</p>
                <p class="description">{{ p.description || 'Premium rental equipment for weddings, corporate galas, & events.' }}</p>
                
                <div class="price-availability-row">
                  <div class="price-tag">
                    <span class="label">Starting from</span>
                    <span class="amount">\${{ p.rentalPrice | number:'1.2-2' }}</span>
                  </div>
                  <div class="avail-badge" [class.available]="p.available" [class.unavailable]="!p.available">
                    {{ p.available ? '✓ ' + p.availableQuantity + ' Available' : 'Out of Stock' }}
                  </div>
                </div>

                <div class="card-actions">
                  <button class="btn-secondary" [routerLink]="['/rentals', p.id]">View Details</button>
                  <button class="btn-primary" (click)="quickAdd(p)">Add to Rental</button>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .storefront-layout {
      min-height: 100vh;
      background: #f8fafc;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .storefront-header {
      background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
      color: white;
      padding: 2.5rem 3rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .brand-title {
      font-size: 2.25rem;
      font-weight: 800;
      margin: 0;
      background: linear-gradient(90deg, #38bdf8, #818cf8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .brand-subtitle {
      color: #94a3b8;
      margin: 0.5rem 0 0 0;
      font-size: 1rem;
    }
    .header-actions {
      display: flex;
      gap: 1rem;
    }
    .cart-btn, .portal-btn {
      padding: 0.75rem 1.25rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
    }
    .cart-btn {
      background: #3b82f6;
      color: white;
    }
    .cart-btn:hover { background: #2563eb; }
    .portal-btn {
      background: rgba(255, 255, 255, 0.1);
      color: white;
      border: 1px solid rgba(255,255,255,0.2);
    }
    .portal-btn:hover { background: rgba(255, 255, 255, 0.2); }
    .catalog-container {
      max-width: 1400px;
      margin: 2rem auto;
      padding: 0 2rem;
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 2rem;
    }
    .filter-sidebar {
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      height: fit-content;
    }
    .filter-sidebar h3 {
      margin-top: 0;
      font-size: 1.15rem;
      color: #0f172a;
    }
    .filter-group {
      margin-bottom: 1.25rem;
    }
    .filter-group label {
      display: block;
      font-size: 0.875rem;
      font-weight: 600;
      color: #475569;
      margin-bottom: 0.35rem;
    }
    .form-control {
      width: 100%;
      padding: 0.6rem 0.75rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.9rem;
      box-sizing: border-box;
    }
    .price-inputs {
      display: flex;
      gap: 0.5rem;
    }
    .product-grid-section {
      display: flex;
      flex-direction: column;
    }
    .grid-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .grid-header h2 {
      margin: 0;
      font-size: 1.5rem;
      color: #0f172a;
    }
    .results-count {
      color: #64748b;
      font-size: 0.9rem;
    }
    .product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.5rem;
    }
    .product-card {
      background: white;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      border: 1px solid #e2e8f0;
      display: flex;
      flex-direction: column;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .product-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
    }
    .card-image-wrapper {
      position: relative;
      height: 180px;
      background: #e2e8f0;
    }
    .card-image-wrapper img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .category-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      background: rgba(15, 23, 42, 0.75);
      color: white;
      padding: 0.25rem 0.6rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .card-content {
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      flex-grow: 1;
    }
    .card-content h3 {
      margin: 0 0 0.25rem 0;
      font-size: 1.1rem;
      color: #0f172a;
    }
    .sku {
      font-size: 0.75rem;
      color: #94a3b8;
      margin: 0 0 0.5rem 0;
    }
    .description {
      font-size: 0.85rem;
      color: #64748b;
      margin-bottom: 1rem;
      line-height: 1.4;
      flex-grow: 1;
    }
    .price-availability-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    .price-tag .label {
      display: block;
      font-size: 0.7rem;
      color: #64748b;
    }
    .price-tag .amount {
      font-size: 1.25rem;
      font-weight: 800;
      color: #0f172a;
    }
    .avail-badge {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.3rem 0.6rem;
      border-radius: 6px;
    }
    .avail-badge.available {
      background: #dcfce7;
      color: #15803d;
    }
    .avail-badge.unavailable {
      background: #fee2e2;
      color: #b91c1c;
    }
    .card-actions {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.5rem;
    }
    .btn-primary {
      background: #3b82f6;
      color: white;
      border: none;
      padding: 0.6rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
    }
    .btn-secondary {
      background: #f1f5f9;
      color: #334155;
      border: 1px solid #cbd5e1;
      padding: 0.6rem;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
    }
    .loading-state, .empty-state {
      text-align: center;
      padding: 4rem;
      background: white;
      border-radius: 12px;
      color: #64748b;
    }

    @media (max-width: 900px) {
      .catalog-container {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class PublicCatalogComponent implements OnInit {
  products: PublicProduct[] = [];
  storefront: TenantStorefront | null = null;
  loading: boolean = true;

  searchTerm: string = '';
  selectedCategory: string = '';
  sortBy: string = 'RECOMMENDED';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  constructor(private storefrontService: StorefrontService, private router: Router) {}

  ngOnInit(): void {
    this.loadStorefrontConfig();
    this.loadCatalog();
  }

  loadStorefrontConfig(): void {
    this.storefrontService.getStorefrontConfig('abc-event-rentals').subscribe({
      next: (config) => this.storefront = config,
      error: () => {}
    });
  }

  loadCatalog(): void {
    this.loading = true;
    this.storefrontService.getCatalog({
      category: this.selectedCategory,
      minPrice: this.minPrice || undefined,
      maxPrice: this.maxPrice || undefined,
      search: this.searchTerm,
      sortBy: this.sortBy
    }).subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.loadCatalog();
  }

  quickAdd(product: PublicProduct): void {
    this.storefrontService.addToCart(product.id, 1).subscribe({
      next: () => {
        this.router.navigate(['/rentals/cart']);
      }
    });
  }
}
