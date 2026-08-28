import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StorefrontService, Cart } from '../services/storefront.service';

@Component({
  selector: 'app-rental-cart',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="cart-layout">
      <header class="cart-header">
        <h1>Your Rental Cart</h1>
        <p>Review items, adjust quantities, & select your checkout method</p>
      </header>

      <div class="cart-container">
        <!-- Warnings Banner -->
        <div class="warning-banner" *ngIf="cart && cart.warnings && cart.warnings.length > 0">
          ⚠️ <strong>Availability Alert:</strong>
          <ul>
            <li *ngFor="let w of cart.warnings">{{ w }}</li>
          </ul>
        </div>

        <div class="cart-grid" *ngIf="cart && cart.items && cart.items.length > 0">
          <!-- Item List Table -->
          <div class="items-card">
            <table class="cart-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Rental Period</th>
                  <th>Quantity</th>
                  <th>Price</th>
                  <th>Subtotal</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of cart.items">
                  <td>
                    <strong>{{ item.productName }}</strong>
                    <span class="sku">SKU: {{ item.sku }}</span>
                  </td>
                  <td>
                    <div class="period-badge">
                      {{ item.startDateTime | date:'MMM d' }} – {{ item.endDateTime | date:'MMM d, yyyy' }}
                    </div>
                  </td>
                  <td>
                    <input type="number" [(ngModel)]="item.quantity" (change)="updateItem(item)" min="1" class="qty-input" />
                  </td>
                  <td>\${{ item.unitPrice | number:'1.2-2' }}</td>
                  <td><strong>\${{ item.lineSubtotal | number:'1.2-2' }}</strong></td>
                  <td>
                    <button class="remove-btn" (click)="removeItem(item.id)">✕ Remove</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Order Summary Card -->
          <div class="summary-card">
            <h3>Estimated Summary</h3>
            
            <div class="summary-row">
              <span>Rental Subtotal</span>
              <span>\${{ cart.subtotal | number:'1.2-2' }}</span>
            </div>

            <div class="summary-row">
              <span>Estimated Tax (8.25%)</span>
              <span>\${{ cart.estimatedTax | number:'1.2-2' }}</span>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row total-row">
              <span>Estimated Total</span>
              <span class="total-amount">\${{ cart.estimatedTotal | number:'1.2-2' }}</span>
            </div>

            <div class="cart-actions">
              <button class="btn-checkout" routerLink="/rentals/checkout">Continue to Booking</button>
              <button class="btn-quote" routerLink="/rentals/request-quote">Request Quote</button>
              <button class="btn-continue" routerLink="/rentals">Continue Shopping</button>
            </div>
          </div>
        </div>

        <!-- Empty Cart -->
        <div class="empty-cart" *ngIf="!cart || !cart.items || cart.items.length === 0">
          <h2>Your Rental Cart is Empty</h2>
          <p>Explore our catalog to add event equipment & check live availability.</p>
          <button class="btn-browse" routerLink="/rentals">Browse Products</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cart-layout {
      min-height: 100vh;
      background: #f8fafc;
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
    }
    .cart-header {
      background: #1e293b;
      color: white;
      padding: 2.5rem 3rem;
      text-align: center;
    }
    .cart-header h1 {
      margin: 0;
      font-size: 2rem;
      font-weight: 800;
    }
    .cart-header p {
      color: #94a3b8;
      margin: 0.5rem 0 0 0;
    }
    .cart-container {
      max-width: 1200px;
      margin: 2rem auto;
      padding: 0 2rem;
    }
    .warning-banner {
      background: #fef3c7;
      border: 1px solid #fcd34d;
      color: #92400e;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;
    }
    .warning-banner ul {
      margin: 0.5rem 0 0 1.25rem;
      padding: 0;
    }
    .cart-grid {
      display: grid;
      grid-template-columns: 1fr 340px;
      gap: 2rem;
    }
    .items-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
      border: 1px solid #cbd5e1;
    }
    .cart-table {
      width: 100%;
      border-collapse: collapse;
    }
    .cart-table th {
      text-align: left;
      font-size: 0.8rem;
      text-transform: uppercase;
      color: #64748b;
      padding-bottom: 1rem;
      border-bottom: 1px solid #e2e8f0;
    }
    .cart-table td {
      padding: 1.25rem 0;
      border-bottom: 1px solid #f1f5f9;
      font-size: 0.9rem;
      vertical-align: middle;
    }
    .sku {
      display: block;
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .period-badge {
      background: #f1f5f9;
      color: #334155;
      padding: 0.35rem 0.6rem;
      border-radius: 6px;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .qty-input {
      width: 60px;
      padding: 0.4rem;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
    }
    .remove-btn {
      background: none;
      border: none;
      color: #ef4444;
      cursor: pointer;
      font-size: 0.8rem;
      font-weight: 600;
    }
    .summary-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
      border: 1px solid #cbd5e1;
      height: fit-content;
    }
    .summary-card h3 {
      margin-top: 0;
      font-size: 1.1rem;
      color: #0f172a;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 0.75rem;
    }
    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.85rem;
      font-size: 0.9rem;
      color: #475569;
    }
    .summary-divider {
      height: 1px;
      background: #e2e8f0;
      margin: 1rem 0;
    }
    .total-row {
      font-weight: 800;
      color: #0f172a;
      font-size: 1.1rem;
    }
    .total-amount {
      color: #2563eb;
      font-size: 1.35rem;
    }
    .cart-actions {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-top: 1.5rem;
    }
    .btn-checkout {
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.85rem;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
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
    .btn-continue {
      background: #f1f5f9;
      color: #334155;
      border: 1px solid #cbd5e1;
      padding: 0.75rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
    }
    .empty-cart {
      text-align: center;
      padding: 5rem 2rem;
      background: white;
      border-radius: 16px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .empty-cart h2 {
      color: #0f172a;
    }
    .empty-cart p {
      color: #64748b;
      margin-bottom: 1.5rem;
    }
    .btn-browse {
      background: #2563eb;
      color: white;
      border: none;
      padding: 0.85rem 1.75rem;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
    }

    @media (max-width: 900px) {
      .cart-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class RentalCartComponent implements OnInit {
  cart: Cart | null = null;

  constructor(private storefrontService: StorefrontService, private router: Router) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.storefrontService.getCart().subscribe({
      next: (data) => this.cart = data
    });
  }

  updateItem(item: any): void {
    this.storefrontService.updateCartItem(item.id, item.quantity).subscribe({
      next: (updated) => this.cart = updated
    });
  }

  removeItem(itemId: string): void {
    this.storefrontService.removeFromCart(itemId).subscribe({
      next: (updated) => this.cart = updated
    });
  }
}
